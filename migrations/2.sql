
-- Make the first user (app owner) an admin automatically
UPDATE user_profiles SET role = 'admin' WHERE id = (SELECT MIN(id) FROM user_profiles);
