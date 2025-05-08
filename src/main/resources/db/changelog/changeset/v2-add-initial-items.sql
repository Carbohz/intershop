-- liquibase formatted sql

-- changeset carbohz:1

INSERT INTO item (title, description, image_path, count, price) VALUES
('Laptop', 'High-performance laptop with 16GB RAM', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=400', 10, 149999),
('Smartphone', 'Latest model smartphone', 'https://images.pexels.com/photos/788946/pexels-photo-788946.jpeg?auto=compress&cs=tinysrgb&w=400', 25, 79999),
('Wireless Headphones', 'Noise-canceling Bluetooth headphones', 'https://cdn.pixabay.com/photo/2017/03/29/15/18/headphones-2185608_1280.jpg', 15, 29999),
('Mechanical Keyboard', 'RGB gaming keyboard', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=400', 30, 12999),
('Gaming Mouse', 'Programmable buttons, 16000 DPI', 'https://images.pexels.com/photos/4439451/pexels-photo-4439451.jpeg?auto=compress&cs=tinysrgb&w=400', 40, 8999),
('4K Monitor', '27-inch IPS display', 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=400', 8, 45999),
('Tablet', '10-inch Android tablet', 'https://images.pexels.com/photos/1334597/pexels-photo-1334597.jpeg?auto=compress&cs=tinysrgb&w=400', 20, 24999),
('Smart Watch', 'Fitness tracker with heart rate monitor', 'https://images.unsplash.com/photo-1558126319-c9feecbf57ee?auto=format&fit=crop&w=400', 35, 19999),
('DSLR Camera', '24MP digital camera', 'https://cdn.pixabay.com/photo/2014/06/27/12/36/camera-578178_1280.jpg', 5, 99999),
('Wireless Printer', 'All-in-one color printer', 'https://images.pexels.com/photos/2651794/pexels-photo-2651794.jpeg?auto=compress&cs=tinysrgb&w=400', 12, 34999),
('Bluetooth Speaker', 'Portable waterproof speaker', 'https://images.unsplash.com/photo-1591378603223-e15b45a81640?auto=format&fit=crop&w=400', 50, 7999),
('Wi-Fi Router', 'Dual-band AC1750', 'https://images.pexels.com/photos/2047905/pexels-photo-2047905.jpeg?auto=compress&cs=tinysrgb&w=400', 18, 8999),
('External SSD', '1TB USB-C portable drive', 'https://images.unsplash.com/photo-1588018025171-0584d3c6d4ac?auto=format&fit=crop&w=400', 7, 12999),
('Gaming Chair', 'Ergonomic racing-style chair', 'https://images.pexels.com/photos/4475523/pexels-photo-4475523.jpeg?auto=compress&cs=tinysrgb&w=400', 3, 29999),
('Desk Lamp', 'Adjustable LED lamp', 'https://images.unsplash.com/photo-1580477667995-2b94f01c9516?auto=format&fit=crop&w=400', 40, 4999),
('Backpack', 'Water-resistant laptop backpack', 'https://images.pexels.com/photos/2905238/pexels-photo-2905238.jpeg?auto=compress&cs=tinysrgb&w=400', 25, 6999),
('Coffee Maker', 'Programmable 12-cup brewer', 'https://cdn.pixabay.com/photo/2017/09/24/17/34/coffee-maker-2781654_1280.jpg', 15, 8999),
('Electric Kettle', '1.7L stainless steel', 'https://images.unsplash.com/photo-1595341883842-e8f6a8e55d80?auto=format&fit=crop&w=400', 30, 3999),
('Fitness Band', 'Activity tracker with GPS', 'https://images.pexels.com/photos/437037/pexels-photo-437037.jpeg?auto=compress&cs=tinysrgb&w=400', 22, 14999),
('Power Bank', '20000mAh portable charger', 'https://images.unsplash.com/photo-1586333250925-87a6d23646c5?auto=format&fit=crop&w=400', 45, 5999);