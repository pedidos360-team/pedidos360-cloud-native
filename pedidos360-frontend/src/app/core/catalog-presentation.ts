import { Product } from './models';

const productImages: Record<string, { url: string; attribution: string }> = {
  'Laptop HP Cloud': {
    url: 'https://images.pexels.com/photos/6968164/pexels-photo-6968164.jpeg',
    attribution: 'Mikhail Nilov on Pexels'
  },
  'Teclado Mecánico RGB': {
    url: 'https://images.pexels.com/photos/28842075/pexels-photo-28842075.jpeg',
    attribution: 'Atahan Demir on Pexels'
  }
};

export function productSku(product: Product): string {
  return product.sku ?? `PRO-${String(product.id).padStart(3, '0')}`;
}

export function productImage(product: Product): string | null {
  return product.imageUrl ?? productImages[product.nombre]?.url ?? null;
}

export function productImageAlt(product: Product): string {
  return product.imageAlt
    ?? `${product.nombre} · imagen de ${productImages[product.nombre]?.attribution ?? 'producto'}`;
}
