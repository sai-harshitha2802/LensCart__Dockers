
import { Component, OnInit } from '@angular/core';
import { Glass, GlassService } from 'src/app/services/glass.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-glass-management',
  templateUrl: './glasses-management.component.html',
  styleUrls: ['./glasses-management.component.css']
})
export class GlassesManagementComponent implements OnInit {

  glasses: Glass[] = [];
  glassData: Glass = this.resetData();
  isEditMode = false;


  constructor(private glassService: GlassService) {}

  ngOnInit(): void {
    this.loadGlasses();
  }

  loadGlasses(): void {
    this.glassService.getAllGlasses().pipe(
      catchError(error => {
        console.error('Error loading glasses:', error);
        return of([]);
      })
    ).subscribe(data => this.glasses = data);
  }

  onSubmit(): void {
    if (this.isEditMode) {
      if (!this.glassData.glassId) {
        console.error('Glass ID is required for update');
        return;
      }
      this.glassService.updateGlass(this.glassData).pipe(
        catchError(error => {
          console.error('Error updating glass:', error);
          return of(null);
        })
      ).subscribe(() => {
        this.loadGlasses();
        this.resetForm();
      });
    } else {
      this.glassService.addGlass(this.glassData).pipe(
        catchError(error => {
          console.error('Error adding glass:', error);
          return of(null);
        })
      ).subscribe(() => {
        this.loadGlasses();
        this.resetForm();
      });
    }
  }

  editGlass(glass: Glass): void {
    this.glassData = { ...glass };
    this.isEditMode = true;
  }

  deleteGlass(glassId: string): void {
    if (confirm('Are you sure you want to delete this glass?')) {
      this.glassService.deleteGlass(glassId).pipe(
        catchError(error => {
          console.error('Error deleting glass:', error);
          return of(null);
        })
      ).subscribe(() => this.loadGlasses());
    }
  }

  resetForm(): void {
    this.glassData = this.resetData();
    this.isEditMode = false;
  }

  resetData(): Glass {
    return {
      glassId: '',
      brand: '',
      glassImage: '',
      glassName: '',
      powerRange: 0,
      type: '',
      price: 0,
      quantity: 0
    };
  }
}
