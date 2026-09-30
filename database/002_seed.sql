-- Base units of measure. Roles and the initial admin user are seeded by the backend on startup.
INSERT INTO "unit_of_measure" ("name", "abbreviation", "type", "conversion_factor") VALUES
  ('Kilogram',  'KG',  'WEIGHT', 1),
  ('Gram',      'G',   'WEIGHT', 0.001),
  ('Liter',     'L',   'VOLUME', 1),
  ('Milliliter','ML',  'VOLUME', 0.001),
  ('Unit',      'UND', 'UNIT',   1)
ON CONFLICT ("abbreviation") DO NOTHING;
