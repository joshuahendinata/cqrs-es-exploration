import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { InventoryComponent } from './inventory/inventory.component';
import { PageNotFoundComponent } from './not-found.component';

const appRoutes: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: 'inventoryPage'
            },
            {
                path: 'inventoryPage',
                component: InventoryComponent
            },
        ]
    },
    { path: '', redirectTo: 'app/inventoryPage', pathMatch: 'full' },
    { path: '**', component: PageNotFoundComponent }
];


@NgModule( {
    declarations: [
        AppComponent,
        InventoryComponent,
        PageNotFoundComponent
    ],
    imports: [
        BrowserModule,
        HttpModule,
        FormsModule,
        RouterModule.forRoot(
            appRoutes,
            {
                enableTracing: true, // <-- debugging purposes only
            }
        ),
        MatTableModule,
        MatPaginatorModule,
        BrowserAnimationsModule
    ],
    providers: [],
    bootstrap: [AppComponent]
} )
export class AppModule { }
