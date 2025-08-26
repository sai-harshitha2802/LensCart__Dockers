import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Glass {
  glassId: string;
  brand: string;
  glassImage: string;
  glassName: string;
  powerRange: number;
  type: string;
  price: number;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class GlassService {

  private baseUrl = 'http://localhost:8084'; // Update with your backend URL

  constructor(private http: HttpClient) {}

  getAllGlasses(): Observable<Glass[]> {
    return this.http.get<Glass[]>(`${this.baseUrl}/all-glasses`);
  }
  addGlass(glass: Glass): Observable<Glass> {
    return this.http.post<Glass>(`${this.baseUrl}/add-glasses`, glass);
  }

  // updateGlass(glass: Glass): Observable<Glass> {
  //   if (!glass.glassId) {
  //     throw new Error('Glass ID is required for update');
  //   }
  //   return this.http.put<Glass>(`${this.baseUrl}/${glass.glassId}`, glass);
  // }

  // deleteGlass(glassId: string): Observable<void> {
  //   if (!glassId) {
  //     throw new Error('Glass ID is required for delete');
  //   }
  //   return this.http.delete<void>(`${this.baseUrl}/${glassId}`);
  // }
updateGlass(glass: Glass): Observable<Glass> {
  if (!glass.glassId) {
    throw new Error('Glass ID is required for update');
  }
  return this.http.put<Glass>(`${this.baseUrl}/${glass.glassId}`, glass);
}

deleteGlass(glassId: string): Observable<void> {
  if (!glassId) {
    throw new Error('Glass ID is required for delete');
  }
  return this.http.delete<void>(`${this.baseUrl}/${glassId}`);
}
}
