import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../core/catalog.service';
import { productImage, productImageAlt, productSku } from '../../core/catalog-presentation';
import { formatCurrency } from '../../core/formatters';
import { AvailabilityFilter, Product } from '../../core/models';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: './productos.css',
  templateUrl: './productos.html'
})
export class Productos implements OnInit {
  private readonly catalogService = inject(CatalogService);
  private readonly cdr = inject(ChangeDetectorRef);

  products: Product[] = [];
  status: 'loading' | 'success' | 'error' = 'loading';
  errorMessage = '';
  searchTerm = '';
  availabilityFilter: AvailabilityFilter = 'TODOS';

  showCreateForm = false;
  isSubmitting = false;
  createError = '';
  newNombre = '';
  newPrecio = '';
  newStock: number | null = 1;

  deletingId: number | null = null;
  deleteError = '';

  updatingStockId: number | null = null;
  stockError = '';

  readonly formatCurrency = formatCurrency;
  readonly productImage = productImage;
  readonly productImageAlt = productImageAlt;
  readonly productSku = productSku;

  ngOnInit(): void {
    this.loadProducts();
  }

  get filteredProducts(): Product[] {
    const query = this.searchTerm.trim().toLocaleLowerCase('es-CL');

    return this.products.filter((product) => {
      const matchesSearch = !query
        || product.nombre.toLocaleLowerCase('es-CL').includes(query)
        || this.productSku(product).toLocaleLowerCase('es-CL').includes(query);
      const matchesAvailability = this.availabilityFilter === 'TODOS'
        || (this.availabilityFilter === 'DISPONIBLES' && product.stock > 0)
        || (this.availabilityFilter === 'SIN_STOCK' && product.stock === 0);

      return matchesSearch && matchesAvailability;
    });
  }

  loadProducts(): void {
    this.status = 'loading';
    this.errorMessage = '';
    this.catalogService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.status = 'success';
        this.cdr.detectChanges();
      },
      error: () => {
        this.status = 'error';
        this.errorMessage = 'No pudimos cargar el catálogo.';
        this.cdr.detectChanges();
      }
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.createError = '';
  }

  submitProduct(): void {
    if (this.isSubmitting) {
      return;
    }

    const nombre = this.newNombre.trim();
    // CLP no tiene decimales: cualquier "." o "," que se haya escrito es un
    // separador de miles (ej. "290.990"), nunca una parte decimal real.
    const precio = Number(this.newPrecio.replace(/[^0-9]/g, ''));
    const stock = Number(this.newStock);

    if (!nombre || !Number.isFinite(precio) || precio <= 0 || !Number.isInteger(stock) || stock < 0) {
      this.createError = 'Completa el nombre, un precio mayor a 0 y un stock entero igual o mayor a 0.';
      return;
    }

    this.isSubmitting = true;
    this.createError = '';

    this.catalogService.createProduct({
      nombre,
      precio,
      stock
    }).subscribe({
      next: (created) => {
        this.products = [created, ...this.products];
        this.status = 'success';
        this.newNombre = '';
        this.newPrecio = '';
        this.newStock = 1;
        this.showCreateForm = false;
        this.isSubmitting = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSubmitting = false;
        this.createError = 'No se pudo agregar el producto. Verifica tu sesión y la conexión con la API.';
        this.cdr.detectChanges();
      }
    });
  }

  deleteProduct(product: Product): void {
    if (this.deletingId !== null) {
      return;
    }

    const confirmado = confirm(`¿Eliminar "${product.nombre}" del catálogo?`);
    if (!confirmado) {
      return;
    }

    this.deletingId = product.id;
    this.deleteError = '';

    this.catalogService.deleteProduct(product.id).subscribe({
      next: () => {
        this.products = this.products.filter((p) => p.id !== product.id);
        this.deletingId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.deletingId = null;
        this.deleteError = 'No se pudo eliminar el producto. Verifica tu sesión y la conexión con la API.';
        this.cdr.detectChanges();
      }
    });
  }

  actualizarStock(product: Product, valor: string): void {
    if (this.updatingStockId !== null) {
      return;
    }

    const stock = Number(valor.replace(/[^0-9]/g, ''));
    if (!Number.isInteger(stock) || stock < 0) {
      this.stockError = 'El stock debe ser un número entero igual o mayor a 0.';
      return;
    }

    this.updatingStockId = product.id;
    this.stockError = '';

    this.catalogService.updateStock(product.id, stock).subscribe({
      next: (actualizado) => {
        product.stock = actualizado.stock;
        this.updatingStockId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.updatingStockId = null;
        this.stockError = 'No se pudo actualizar el stock. Verifica tu sesión y la conexión con la API.';
        this.cdr.detectChanges();
      }
    });
  }

  onImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.hidden = true;
    image.parentElement?.classList.add('product-media--error');
  }
}