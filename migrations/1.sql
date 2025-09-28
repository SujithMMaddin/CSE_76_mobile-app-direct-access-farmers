
CREATE TABLE user_profiles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL UNIQUE,
  role TEXT NOT NULL CHECK (role IN ('farmer', 'buyer', 'admin')),
  name TEXT NOT NULL,
  phone TEXT,
  location TEXT,
  upi_id TEXT,
  profile_image_url TEXT,
  is_verified BOOLEAN DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crops (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  farmer_id TEXT NOT NULL,
  crop_name TEXT NOT NULL,
  variety TEXT,
  total_quantity INTEGER NOT NULL,
  available_quantity INTEGER NOT NULL,
  lot_size INTEGER NOT NULL,
  base_price_per_kg REAL NOT NULL,
  harvest_date DATE,
  auction_ends_at TIMESTAMP NOT NULL,
  location TEXT,
  latitude REAL,
  longitude REAL,
  status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'SOLD', 'CLOSED')),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crop_images (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  crop_id INTEGER NOT NULL,
  image_url TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bids (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  crop_id INTEGER NOT NULL,
  buyer_id TEXT NOT NULL,
  bid_amount_per_kg REAL NOT NULL,
  bid_quantity INTEGER NOT NULL,
  status TEXT NOT NULL DEFAULT 'PLACED' CHECK (status IN ('PLACED', 'ACCEPTED', 'REJECTED')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  crop_id INTEGER NOT NULL,
  buyer_id TEXT NOT NULL,
  farmer_id TEXT NOT NULL,
  final_price_per_kg REAL NOT NULL,
  quantity INTEGER NOT NULL,
  total_amount REAL NOT NULL,
  payment_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED')),
  payment_gateway_response TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_crops_farmer_id ON crops(farmer_id);
CREATE INDEX idx_crops_status ON crops(status);
CREATE INDEX idx_crop_images_crop_id ON crop_images(crop_id);
CREATE INDEX idx_bids_crop_id ON bids(crop_id);
CREATE INDEX idx_bids_buyer_id ON bids(buyer_id);
CREATE INDEX idx_transactions_buyer_id ON transactions(buyer_id);
CREATE INDEX idx_transactions_farmer_id ON transactions(farmer_id);
