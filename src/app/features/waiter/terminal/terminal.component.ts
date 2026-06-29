import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-waiter-terminal',
  standalone: true,
  templateUrl: './terminal.component.html',
  styleUrl: './terminal.component.scss'
})
export class WaiterTerminalComponent {
  private route = inject(ActivatedRoute);
  
  public tableId: string | null = null;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.tableId = params['tableId'] || null;
    });
  }
}
