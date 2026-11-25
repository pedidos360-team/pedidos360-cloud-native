import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
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

  products: Product[] = [];
  status: 'loading' | 'success' | 'error' = 'loading';
  errorMessage = '';
  searchTerm = '';
  availabilityFilter: AvailabilityFilter = 'TODOS';

  showCreateForm = false;
  isSubmitting = false;
  createError = '';
  newNombre = '';
  newPrecio: number | null = null;
  newStock: number | null = 1;

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
      },
      error: () => {
        this.status = 'error';
        this.errorMessage = 'No pudimos cargar el catálogo.';
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
    const precio = Number(this.newPrecio);
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
        this.newPrecio = null;
        this.newStock = 1;
        this.showCreateForm = false;
        this.isSubmitting = false;
      },
      error: () => {
        this.isSubmitting = false;
        this.createError = 'No se pudo agregar el producto. Verifica tu sesión y la conexión con la API.';
      }
    });
  }

  onImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.hidden = true;
    image.parentElement?.classList.add('product-media--error');
  }
}