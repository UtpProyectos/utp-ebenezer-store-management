export const TODAY = new Date('2026-09-29T12:00:00');
const P = (id, name, cat, code, unit, stock, min, price, cost, exp, prov, sold) => ({ id, name, cat, code, unit, stock, min, price, cost, exp, prov, sold, active: true });
export const products = [
  P(1, 'Gaseosa cola 500 ml', 'Bebidas', '7750001000011', 'unid.', 34, 24, 2.5, 1.6, '2027-03-10', 'Distribuidora San Martín', 86),
  P(2, 'Agua mineral 625 ml', 'Bebidas', '7750001000028', 'unid.', 6, 24, 1.5, 0.85, '2027-06-01', 'Distribuidora San Martín', 64),
  P(3, 'Leche evaporada 400 g', 'Lácteos', '7750001000035', 'lata', 18, 20, 4.2, 3.4, '2026-10-04', 'Lácteos del Valle', 58),
  P(4, 'Yogurt de fresa 1 L', 'Lácteos', '7750001000042', 'botella', 5, 8, 6.5, 4.9, '2026-10-02', 'Lácteos del Valle', 21),
  P(5, 'Queso fresco 250 g', 'Lácteos', '7750001000059', 'unid.', 3, 6, 8.0, 6.0, '2026-09-27', 'Lácteos del Valle', 9),
  P(6, 'Arroz extra 1 kg', 'Abarrotes', '7750001000066', 'bolsa', 42, 20, 4.8, 3.9, null, 'Makro Independencia', 51),
  P(7, 'Azúcar rubia 1 kg', 'Abarrotes', '7750001000073', 'bolsa', 9, 15, 4.0, 3.2, null, 'Makro Independencia', 38),
  P(8, 'Aceite vegetal 1 L', 'Abarrotes', '7750001000080', 'botella', 4, 12, 9.5, 7.8, '2027-08-01', 'Makro Independencia', 27),
  P(9, 'Fideos spaghetti 500 g', 'Abarrotes', '7750001000097', 'paquete', 26, 15, 3.2, 2.4, '2027-11-01', 'Makro Independencia', 33),
  P(10, 'Atún en lata 170 g', 'Abarrotes', '7750001000103', 'lata', 14, 10, 6.0, 4.6, '2028-02-01', 'Distribuidora San Martín', 19),
  P(11, 'Galletas de soda x6', 'Snacks', '7750001000110', 'paquete', 22, 12, 1.2, 0.75, '2027-01-15', 'Distribuidora San Martín', 47),
  P(12, 'Papas fritas 45 g', 'Snacks', '7750001000127', 'bolsa', 5, 12, 2.0, 1.3, '2026-12-20', 'Distribuidora San Martín', 41),
  P(13, 'Chocolate con maní 30 g', 'Snacks', '7750001000134', 'unid.', 13, 15, 1.5, 0.95, '2027-02-10', 'Distribuidora San Martín', 44),
  P(14, 'Detergente 500 g', 'Limpieza', '7750001000141', 'bolsa', 12, 8, 5.5, 4.2, null, 'Makro Independencia', 12),
  P(15, 'Lejía 1 L', 'Limpieza', '7750001000158', 'botella', 2, 6, 3.5, 2.4, null, 'Makro Independencia', 10),
  P(16, 'Papel higiénico x4', 'Limpieza', '7750001000165', 'paquete', 16, 10, 6.9, 5.4, null, 'Makro Independencia', 15),
  P(17, 'Cuaderno A4 cuadriculado', 'Librería', '7750001000172', 'unid.', 20, 10, 5.0, 3.2, null, 'Librería Amauta', 8),
  P(18, 'Lapicero azul', 'Librería', '7750001000189', 'unid.', 48, 20, 1.0, 0.45, null, 'Librería Amauta', 17),
  { ...P(19, 'Pan francés', 'Panadería', '', 'unid.', 60, 40, 0.4, 0.18, null, 'Panadería La Espiga', 240), promo: { n: 3, price: 1 } },
  P(20, 'Huevos rosados', 'Abarrotes', '', 'kg', 6.5, 4, 8.5, 6.8, '2026-10-15', 'Mercado Central', 42),
  P(21, 'Arroz suelto', 'Abarrotes', '', 'kg', 18, 10, 4.2, 3.4, null, 'Makro Independencia', 55),
];
export const categories = [
  { name: 'Bebidas', icon: 'local_drink', desc: 'Gaseosas, aguas y jugos', active: true },
  { name: 'Lácteos', icon: 'egg_alt', desc: 'Leche, yogurt y quesos', active: true },
  { name: 'Snacks', icon: 'cookie', desc: 'Galletas, papas y dulces', active: true },
  { name: 'Limpieza', icon: 'soap', desc: 'Hogar y aseo', active: true },
  { name: 'Librería', icon: 'edit_note', desc: 'Útiles escolares', active: true },
  { name: 'Abarrotes', icon: 'grocery', desc: 'Arroz, azúcar, aceite, conservas', active: true },
  { name: 'Panadería', icon: 'bakery_dining', desc: 'Pan del día', active: true },
  { name: 'Licores', icon: 'liquor', desc: 'Sin productos por ahora', active: false },
];
export const providers = [
  { name: 'Distribuidora San Martín', type: 'Mayorista', contact: 'Jorge Salas', phone: '987 654 321', address: 'Av. Los Olivos 1240, SMP', last: '26 sep', count: 18, active: true, note: 'Visita martes y viernes' },
  { name: 'Makro Independencia', type: 'Autoservicio', contact: '—', phone: '(01) 612 3400', address: 'Av. Alfredo Mendiola 3698', last: '24 sep', count: 14, active: true, note: 'Compra con tarjeta' },
  { name: 'Lácteos del Valle', type: 'Distribuidor', contact: 'Carmen Ríos', phone: '945 112 870', address: '', last: '22 sep', count: 6, active: true, note: 'Pedido mínimo S/ 150' },
  { name: 'Mercado Central', type: 'Mercado', contact: 'Puesto 45 · Don Tito', phone: '912 004 551', address: 'Jr. Ucayali 700', last: '27 sep', count: 9, active: true, note: '' },
  { name: 'Panadería La Espiga', type: 'Local', contact: 'Sra. Elena', phone: '956 330 118', address: 'Calle Los Pinos 118', last: 'Hoy', count: 42, active: true, note: 'Entrega diaria 6:00 a. m.' },
  { name: 'Librería Amauta', type: 'Mayorista', contact: 'Oficina', phone: '(01) 428 1190', address: 'Jr. Amazonas 390', last: '02 mar', count: 3, active: false, note: 'Solo temporada escolar' },
];
export const users = [
  { name: 'Rosa Quispe', user: 'rosa.quispe', role: 'Administrador', active: true, last: 'Ahora', ini: 'RQ' },
  { name: 'Luis Huamán', user: 'luis.h', role: 'Cajero', active: true, last: 'Hoy, 07:02', ini: 'LH' },
  { name: 'Andrea Flores', user: 'andrea.f', role: 'Cajero', active: true, last: 'Ayer, 21:15', ini: 'AF' },
  { name: 'María Torres', user: 'maria.t', role: 'Cajero', active: false, last: '12 ago', ini: 'MT' },
];
export const sales7 = [
  { d: 'Mié', v: 398 }, { d: 'Jue', v: 452 }, { d: 'Vie', v: 521 }, { d: 'Sáb', v: 610 }, { d: 'Dom', v: 575 }, { d: 'Lun', v: 430 }, { d: 'Hoy', v: 486.4 },
];
export const activity = [
  { t: '11:42', who: 'Luis H.', txt: 'Venta #1038', amt: 'S/ 18.50', icon: 'point_of_sale' },
  { t: '11:15', who: 'Rosa Q.', txt: 'Ingreso de lote · Arroz extra 1 kg ×20', amt: 'S/ 78.00', icon: 'local_shipping' },
  { t: '10:58', who: 'Sistema', txt: 'Queso fresco 250 g venció el 27 sep', amt: '', icon: 'warning' },
  { t: '10:31', who: 'Luis H.', txt: 'Venta #1037', amt: 'S/ 6.80', icon: 'point_of_sale' },
  { t: '09:50', who: 'Rosa Q.', txt: 'Consumo interno · Pan francés ×6', amt: 'S/ 0.00', icon: 'output' },
  { t: '06:10', who: 'Rosa Q.', txt: 'Ingreso de lote · Pan francés ×120', amt: 'S/ 21.60', icon: 'local_shipping' },
];
export const purchases = [
  { date: 'Hoy 11:15', prod: 'Arroz extra 1 kg', qty: '20 bolsas', prov: 'Makro Independencia', total: 78, lote: 'L-0926-A' },
  { date: 'Hoy 06:10', prod: 'Pan francés', qty: '120 unid.', prov: 'Panadería La Espiga', total: 21.6, lote: 'L-0929-P' },
  { date: '27 sep', prod: 'Huevos rosados', qty: '90 unid.', prov: 'Mercado Central', total: 40.5, lote: 'L-0927-H' },
  { date: '26 sep', prod: 'Gaseosa cola 500 ml', qty: '48 unid.', prov: 'Distribuidora San Martín', total: 76.8, lote: 'L-0926-G' },
];
export const consumos = [
  { date: 'Hoy 09:50', prod: 'Pan francés', qty: 6, reason: 'Consumo del personal', who: 'Rosa Quispe', cost: 1.08 },
  { date: '28 sep', prod: 'Lejía 1 L', qty: 1, reason: 'Limpieza del local', who: 'Luis Huamán', cost: 2.4 },
  { date: '27 sep', prod: 'Queso fresco 250 g', qty: 2, reason: 'Merma / vencido', who: 'Rosa Quispe', cost: 12.0 },
  { date: '25 sep', prod: 'Agua mineral 625 ml', qty: 4, reason: 'Consumo del personal', who: 'Andrea Flores', cost: 3.4 },
];
export const money = n => 'S/ ' + (Number(n) || 0).toFixed(2);
export const daysTo = exp => exp ? Math.round((new Date(exp + 'T12:00:00') - TODAY) / 86400000) : null;
export const fmtDate = exp => { if (!exp) return '—'; const d = new Date(exp + 'T12:00:00'); return d.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' }).replace('.', ''); };
export const STATUS = {
  normal: { label: 'Bien', fg: '#1E6A40', bg: '#E4F3EA', dot: '#2F8A57' },
  bajo: { label: 'Quedan pocos', fg: '#8A5200', bg: '#FFF1D9', dot: '#D98E1A' },
  critico: { label: 'Crítico', fg: '#E5383B', bg: '#FDE7EA', dot: '#EF3E42' },
  vence: { label: 'Vence pronto', fg: '#4A5361', bg: '#EEF0F3', dot: '#6B7585' },
  vencido: { label: 'Vencido', fg: '#FFFFFF', bg: '#E5383B', dot: '#FFFFFF' },
};
export const status = p => {
  const dt = daysTo(p.exp);
  if (dt !== null && dt < 0) return 'vencido';
  if (p.stock <= p.min * 0.5) return 'critico';
  if (dt !== null && dt <= 10) return 'vence';
  if (p.stock <= p.min) return 'bajo';
  return 'normal';
};
export const suggested = p => Math.max(0, Math.ceil(p.min * 2 - p.stock));
