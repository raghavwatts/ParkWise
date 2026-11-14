-- Run this in MySQL Workbench or MySQL command line
-- This adds 14 more vehicles to reach a total of 21

USE parkwise_db;

INSERT INTO vehicles (registration_number, vehicle_type, owner_name, contact_number) VALUES
('TN07OP3456','TWO_WHEELER', 'Karthik Raj',   '9123456780'),
('GJ05QR7890','FOUR_WHEELER','Deepa Menon',   '9234567801'),
('RJ03ST1234','TWO_WHEELER', 'Vijay Reddy',   '9345678012'),
('MH12UV5678','FOUR_WHEELER','Sneha Kulkarni','9456780123'),
('KA09WX9012','ACCESSIBLE',  'Ramesh Iyer',   '9567801234'),
('UP16YZ3456','TWO_WHEELER', 'Pooja Verma',   '9678012345'),
('WB20AB7890','FOUR_WHEELER','Amit Bose',     '9780123456'),
('AP11CD1234','TWO_WHEELER', 'Lakshmi Devi',  '9801234567'),
('KL08EF5678','FOUR_WHEELER','Sanjay Nambiar','9812345678'),
('HR26GH9012','TWO_WHEELER', 'Neha Gupta',    '9823456789'),
('PB10IJ3456','FOUR_WHEELER','Harpreet Singh','9834567890'),
('MP09KL7890','TWO_WHEELER', 'Divya Tiwari',  '9845678901'),
('CG04MN2345','FOUR_WHEELER','Rahul Mishra',  '9856789012'),
('OD15OP6789','ACCESSIBLE',  'Sunita Patnaik','9867890123');
