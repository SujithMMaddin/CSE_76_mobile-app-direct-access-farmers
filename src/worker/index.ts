import { Hono } from "hono";
import { cors } from "hono/cors";
import { zValidator } from "@hono/zod-validator";
import { z } from "zod";

interface User {
  id: string;
  email: string;
  name: string;
  picture: string;
}

interface Env {
  DB: D1Database;
}

const FIREBASE_API_KEY = "AIzaSyBxczHU-2IrwXm7vADULDBb1gHA_V4SFhY";

const verifyIdToken = async (idToken: string) => {
  const response = await fetch(
    `https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FIREBASE_API_KEY}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ idToken }),
    }
  );

  if (!response.ok) {
    throw new Error("Invalid token");
  }

  const data: any = await response.json();
  const user = data.users?.[0];
  if (!user) {
    throw new Error("Invalid token");
  }

  return {
    uid: user.localId,
    email: user.email,
    name: user.displayName,
    picture: user.photoUrl,
  };
};

// Firebase Auth middleware
const firebaseAuthMiddleware = async (c: any, next: () => Promise<void>) => {
  const authHeader = c.req.header("Authorization");
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return c.json({ error: "Unauthorized" }, 401);
  }

  const idToken = authHeader.split("Bearer ")[1];
  try {
    const decodedToken = await verifyIdToken(idToken);
    c.set("user", {
      id: decodedToken.uid,
      email: decodedToken.email,
      name: decodedToken.name,
      picture: decodedToken.picture,
    });
    await next();
  } catch {
    return c.json({ error: "Invalid token" }, 401);
  }
};

const app = new Hono<{ Bindings: Env; Variables: { user: User } }>();

app.use(
  "*",
  cors({
    origin: "*",
    allowMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allowHeaders: ["Content-Type", "Authorization"],
  })
);

app.get("/api/users/me", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  // Get user profile from database
  const profile = await c.env.DB.prepare(
    "SELECT * FROM user_profiles WHERE user_id = ?"
  )
    .bind(user.id)
    .first();

  return c.json({
    ...user,
    profile: profile || null,
  });
});

app.get("/api/logout", async (c) => {
  // Firebase Auth logout is handled on the client side
  return c.json({ success: true }, 200);
});

// User profile routes
const ProfileSchema = z.object({
  role: z.enum(["farmer", "buyer"]),
  name: z.string().min(1),
  phone: z.string().optional(),
  location: z.string().optional(),
  upi_id: z.string().optional(),
});

app.post(
  "/api/profile",
  firebaseAuthMiddleware,
  zValidator("json", ProfileSchema),
  async (c) => {
    const user = c.get("user");
    if (!user) {
      return c.json({ error: "User not authenticated" }, 401);
    }
    const { role, name, phone, location, upi_id } = c.req.valid("json");

    const existing = await c.env.DB.prepare(
      "SELECT id FROM user_profiles WHERE user_id = ?"
    )
      .bind(user.id)
      .first();

    if (existing) {
      await c.env.DB.prepare(
        "UPDATE user_profiles SET role = ?, name = ?, phone = ?, location = ?, upi_id = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?"
      )
        .bind(role, name, phone, location, upi_id, user.id)
        .run();
    } else {
      await c.env.DB.prepare(
        "INSERT INTO user_profiles (user_id, role, name, phone, location, upi_id) VALUES (?, ?, ?, ?, ?, ?)"
      )
        .bind(user.id, role, name, phone, location, upi_id)
        .run();
    }

    return c.json({ success: true });
  }
);

// Crop listing routes
const CropSchema = z.object({
  crop_name: z.string().min(1),
  variety: z.string().optional(),
  total_quantity: z.number().positive(),
  lot_size: z.number().positive(),
  base_price_per_kg: z.number().positive(),
  harvest_date: z.string().optional(),
  auction_ends_at: z.string(),
  location: z.string().optional(),
  description: z.string().optional(),
});

app.post(
  "/api/crops",
  firebaseAuthMiddleware,
  zValidator("json", CropSchema),
  async (c) => {
    const user = c.get("user");
    if (!user) {
      return c.json({ error: "User not authenticated" }, 401);
    }
    const cropData = c.req.valid("json");

    // Verify user is a farmer
    const profile = await c.env.DB.prepare(
      "SELECT role FROM user_profiles WHERE user_id = ?"
    )
      .bind(user.id)
      .first();

    if (!profile || profile.role !== "farmer") {
      return c.json({ error: "Only farmers can create crop listings" }, 403);
    }

    const result = await c.env.DB.prepare(
      `INSERT INTO crops (farmer_id, crop_name, variety, total_quantity, available_quantity, 
     lot_size, base_price_per_kg, harvest_date, auction_ends_at, location, description) 
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
    )
      .bind(
        user.id,
        cropData.crop_name,
        cropData.variety,
        cropData.total_quantity,
        cropData.total_quantity, // available_quantity starts as total_quantity
        cropData.lot_size,
        cropData.base_price_per_kg,
        cropData.harvest_date,
        cropData.auction_ends_at,
        cropData.location,
        cropData.description
      )
      .run();

    return c.json({ cropId: result.meta.last_row_id });
  }
);

app.get("/api/crops", async (c) => {
  const status = c.req.query("status") || "OPEN";
  const crops = await c.env.DB.prepare(
    `SELECT c.*, up.name as farmer_name, up.location as farmer_location 
     FROM crops c 
     JOIN user_profiles up ON c.farmer_id = up.user_id 
     WHERE c.status = ? 
     ORDER BY c.created_at DESC`
  )
    .bind(status)
    .all();

  return c.json(crops.results);
});

app.get("/api/crops/:id", async (c) => {
  const cropId = c.req.param("id");

  const crop = await c.env.DB.prepare(
    `SELECT c.*, up.name as farmer_name, up.location as farmer_location 
     FROM crops c 
     JOIN user_profiles up ON c.farmer_id = up.user_id 
     WHERE c.id = ?`
  )
    .bind(cropId)
    .first();

  if (!crop) {
    return c.json({ error: "Crop not found" }, 404);
  }

  // Get bids for this crop
  const bids = await c.env.DB.prepare(
    `SELECT b.*, up.name as buyer_name 
     FROM bids b 
     JOIN user_profiles up ON b.buyer_id = up.user_id 
     WHERE b.crop_id = ? 
     ORDER BY b.bid_amount_per_kg DESC, b.created_at ASC`
  )
    .bind(cropId)
    .all();

  return c.json({ ...crop, bids: bids.results });
});

// Bidding routes
const BidSchema = z.object({
  bid_amount_per_kg: z.number().positive(),
  bid_quantity: z.number().positive(),
});

app.post(
  "/api/crops/:id/bids",
  firebaseAuthMiddleware,
  zValidator("json", BidSchema),
  async (c) => {
    const user = c.get("user");
    if (!user) {
      return c.json({ error: "User not authenticated" }, 401);
    }
    const cropId = c.req.param("id");
    const { bid_amount_per_kg, bid_quantity } = c.req.valid("json");

    // Verify user is a buyer
    const profile = await c.env.DB.prepare(
      "SELECT role FROM user_profiles WHERE user_id = ?"
    )
      .bind(user.id)
      .first();

    if (!profile || profile.role !== "buyer") {
      return c.json({ error: "Only buyers can place bids" }, 403);
    }

    // Get crop details
    const crop = await c.env.DB.prepare(
      "SELECT * FROM crops WHERE id = ? AND status = 'OPEN'"
    )
      .bind(cropId)
      .first();

    if (!crop) {
      return c.json({ error: "Crop not found or auction closed" }, 404);
    }

    // Validate bid
    if (bid_amount_per_kg < Number(crop.base_price_per_kg)) {
      return c.json({ error: "Bid must be at least the base price" }, 400);
    }

    if (bid_quantity > Number(crop.available_quantity)) {
      return c.json({ error: "Bid quantity exceeds available quantity" }, 400);
    }

    // Check if auction has ended
    if (new Date(String(crop.auction_ends_at)) < new Date()) {
      return c.json({ error: "Auction has ended" }, 400);
    }

    const result = await c.env.DB.prepare(
      "INSERT INTO bids (crop_id, buyer_id, bid_amount_per_kg, bid_quantity) VALUES (?, ?, ?, ?)"
    )
      .bind(cropId, user.id, bid_amount_per_kg, bid_quantity)
      .run();

    return c.json({ bidId: result.meta.last_row_id });
  }
);

// Accept bid (farmer only)
app.post("/api/bids/:id/accept", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }
  const bidId = c.req.param("id");

  const bid = await c.env.DB.prepare(
    `SELECT b.*, c.farmer_id, c.available_quantity, c.status as crop_status
     FROM bids b 
     JOIN crops c ON b.crop_id = c.id 
     WHERE b.id = ?`
  )
    .bind(bidId)
    .first();

  if (!bid) {
    return c.json({ error: "Bid not found" }, 404);
  }

  if (bid.farmer_id !== user.id) {
    return c.json({ error: "Only the crop owner can accept bids" }, 403);
  }

  if (bid.crop_status !== "OPEN") {
    return c.json({ error: "Auction is closed" }, 400);
  }

  if (Number(bid.bid_quantity) > Number(bid.available_quantity)) {
    return c.json({ error: "Insufficient quantity available" }, 400);
  }

  // Accept the bid and update quantities
  await c.env.DB.prepare("UPDATE bids SET status = 'ACCEPTED' WHERE id = ?")
    .bind(bidId)
    .run();

  const newAvailableQuantity =
    Number(bid.available_quantity) - Number(bid.bid_quantity);
  const newStatus = newAvailableQuantity <= 0 ? "SOLD" : "OPEN";

  await c.env.DB.prepare(
    "UPDATE crops SET available_quantity = ?, status = ? WHERE id = ?"
  )
    .bind(newAvailableQuantity, newStatus, bid.crop_id)
    .run();

  // Create transaction
  const totalAmount = Number(bid.bid_amount_per_kg) * Number(bid.bid_quantity);
  await c.env.DB.prepare(
    "INSERT INTO transactions (crop_id, buyer_id, farmer_id, final_price_per_kg, quantity, total_amount) VALUES (?, ?, ?, ?, ?, ?)"
  )
    .bind(
      bid.crop_id,
      bid.buyer_id,
      bid.farmer_id,
      bid.bid_amount_per_kg,
      bid.bid_quantity,
      totalAmount
    )
    .run();

  return c.json({ success: true });
});

// Payment simulation
app.post("/api/transactions/:id/pay", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }
  const transactionId = c.req.param("id");

  const transaction = await c.env.DB.prepare(
    "SELECT * FROM transactions WHERE id = ? AND buyer_id = ?"
  )
    .bind(transactionId, user.id)
    .first();

  if (!transaction) {
    return c.json({ error: "Transaction not found" }, 404);
  }

  // Simulate payment success (90% success rate for demo)
  const paymentSuccess = Math.random() > 0.1;
  const paymentStatus = paymentSuccess ? "SUCCESS" : "FAILED";
  const gatewayResponse = JSON.stringify({
    payment_id: `pay_${Date.now()}`,
    status: paymentStatus,
    amount: transaction.total_amount,
    timestamp: new Date().toISOString(),
  });

  await c.env.DB.prepare(
    "UPDATE transactions SET payment_status = ?, payment_gateway_response = ? WHERE id = ?"
  )
    .bind(paymentStatus, gatewayResponse, transactionId)
    .run();

  return c.json({
    success: paymentSuccess,
    paymentStatus,
    gatewayResponse: JSON.parse(gatewayResponse),
  });
});

// Get user's transactions
app.get("/api/transactions", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  const transactions = await c.env.DB.prepare(
    `SELECT t.*, c.crop_name, c.variety, 
     farmer.name as farmer_name, buyer.name as buyer_name
     FROM transactions t
     JOIN crops c ON t.crop_id = c.id
     JOIN user_profiles farmer ON t.farmer_id = farmer.user_id
     JOIN user_profiles buyer ON t.buyer_id = buyer.user_id
     WHERE t.buyer_id = ? OR t.farmer_id = ?
     ORDER BY t.created_at DESC`
  )
    .bind(user.id, user.id)
    .all();

  return c.json(transactions.results);
});

// Admin routes - for app owner to view all data
app.get("/api/admin/stats", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  // Check if user is admin (you can set this in database for your account)
  const profile = await c.env.DB.prepare(
    "SELECT role FROM user_profiles WHERE user_id = ?"
  )
    .bind(user.id)
    .first();

  if (!profile || profile.role !== "admin") {
    return c.json({ error: "Admin access required" }, 403);
  }

  // Get overall statistics
  const userCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM user_profiles"
  ).first();
  const farmerCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM user_profiles WHERE role = 'farmer'"
  ).first();
  const buyerCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM user_profiles WHERE role = 'buyer'"
  ).first();
  const cropCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM crops"
  ).first();
  const bidCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM bids"
  ).first();
  const transactionCount = await c.env.DB.prepare(
    "SELECT COUNT(*) as count FROM transactions"
  ).first();
  const totalRevenue = await c.env.DB.prepare(
    "SELECT SUM(total_amount) as revenue FROM transactions WHERE payment_status = 'SUCCESS'"
  ).first();

  return c.json({
    users: {
      total: userCount?.count || 0,
      farmers: farmerCount?.count || 0,
      buyers: buyerCount?.count || 0,
    },
    crops: cropCount?.count || 0,
    bids: bidCount?.count || 0,
    transactions: transactionCount?.count || 0,
    totalRevenue: totalRevenue?.revenue || 0,
  });
});

app.get("/api/admin/users", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  const profile = await c.env.DB.prepare(
    "SELECT role FROM user_profiles WHERE user_id = ?"
  )
    .bind(user.id)
    .first();

  if (!profile || profile.role !== "admin") {
    return c.json({ error: "Admin access required" }, 403);
  }

  const users = await c.env.DB.prepare(
    "SELECT * FROM user_profiles ORDER BY created_at DESC"
  ).all();

  return c.json(users.results);
});

app.get("/api/admin/crops", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  const profile = await c.env.DB.prepare(
    "SELECT role FROM user_profiles WHERE user_id = ?"
  )
    .bind(user.id)
    .first();

  if (!profile || profile.role !== "admin") {
    return c.json({ error: "Admin access required" }, 403);
  }

  const crops = await c.env.DB.prepare(
    `SELECT c.*, up.name as farmer_name, up.location as farmer_location 
     FROM crops c 
     JOIN user_profiles up ON c.farmer_id = up.user_id 
     ORDER BY c.created_at DESC`
  ).all();

  return c.json(crops.results);
});

app.get("/api/admin/transactions", firebaseAuthMiddleware, async (c) => {
  const user = c.get("user");
  if (!user) {
    return c.json({ error: "User not authenticated" }, 401);
  }

  const profile = await c.env.DB.prepare(
    "SELECT role FROM user_profiles WHERE user_id = ?"
  )
    .bind(user.id)
    .first();

  if (!profile || profile.role !== "admin") {
    return c.json({ error: "Admin access required" }, 403);
  }

  const transactions = await c.env.DB.prepare(
    `SELECT t.*, c.crop_name, c.variety, 
     farmer.name as farmer_name, buyer.name as buyer_name
     FROM transactions t
     JOIN crops c ON t.crop_id = c.id
     JOIN user_profiles farmer ON t.farmer_id = farmer.user_id
     JOIN user_profiles buyer ON t.buyer_id = buyer.user_id
     ORDER BY t.created_at DESC`
  ).all();

  return c.json(transactions.results);
});

export default app;
