import z from "zod";

export const UserProfileSchema = z.object({
  id: z.string(),
  user_id: z.string(),
  role: z.enum(["farmer", "buyer", "admin"]),
  name: z.string(),
  phone: z.string().optional(),
  location: z.string().optional(),
  upi_id: z.string().optional(),
  profile_image_url: z.string().optional(),
  is_verified: z.boolean(),
  created_at: z.string(),
  updated_at: z.string(),
});

export const CropSchema = z.object({
  id: z.number(),
  farmer_id: z.string(),
  crop_name: z.string(),
  variety: z.string().optional(),
  total_quantity: z.number(),
  available_quantity: z.number(),
  lot_size: z.number(),
  base_price_per_kg: z.number(),
  harvest_date: z.string().optional(),
  auction_ends_at: z.string(),
  location: z.string().optional(),
  latitude: z.number().optional(),
  longitude: z.number().optional(),
  status: z.enum(["OPEN", "SOLD", "CLOSED"]),
  description: z.string().optional(),
  created_at: z.string(),
  updated_at: z.string(),
  farmer_name: z.string().optional(),
  farmer_location: z.string().optional(),
});

export const BidSchema = z.object({
  id: z.number(),
  crop_id: z.number(),
  buyer_id: z.string(),
  bid_amount_per_kg: z.number(),
  bid_quantity: z.number(),
  status: z.enum(["PLACED", "ACCEPTED", "REJECTED"]),
  created_at: z.string(),
  updated_at: z.string(),
  buyer_name: z.string().optional(),
});

export const TransactionSchema = z.object({
  id: z.number(),
  crop_id: z.number(),
  buyer_id: z.string(),
  farmer_id: z.string(),
  final_price_per_kg: z.number(),
  quantity: z.number(),
  total_amount: z.number(),
  payment_status: z.enum(["PENDING", "SUCCESS", "FAILED"]),
  payment_gateway_response: z.string().optional(),
  created_at: z.string(),
  updated_at: z.string(),
  crop_name: z.string().optional(),
  variety: z.string().optional(),
  farmer_name: z.string().optional(),
  buyer_name: z.string().optional(),
});

export type UserProfile = z.infer<typeof UserProfileSchema>;
export type Crop = z.infer<typeof CropSchema>;
export type Bid = z.infer<typeof BidSchema>;
export type Transaction = z.infer<typeof TransactionSchema>;

export interface CropWithBids extends Crop {
  bids: Bid[];
}
