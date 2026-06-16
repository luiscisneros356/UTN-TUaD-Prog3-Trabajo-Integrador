// Tipos del dominio Food Store (alineados con el modelo del backend y los JSON).

export type Rol = "ADMIN" | "USUARIO";

export type EstadoPedido =
  | "PENDIENTE"
  | "EN_PREPARACION"
  | "ENTREGADO"
  | "CANCELADO";

export type FormaPago = "TARJETA" | "TRANSFERENCIA" | "EFECTIVO";

export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface Producto {
  id: number;
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagen: string;
  disponible: boolean;
  categoria: Categoria;
}

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  mail: string;
  celular: string;
  rol: Rol;
  password?: string;
}

export interface DetallePedido {
  cantidad: number;
  subtotal: number;
  producto: Producto;
}

export interface Pedido {
  id: number;
  fecha: string;
  estado: EstadoPedido;
  total: number;
  formaPago: FormaPago;
  detalles: DetallePedido[];
  usuarioDto: Usuario;
}

// Ítem del carrito (se guarda en localStorage).
export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}
