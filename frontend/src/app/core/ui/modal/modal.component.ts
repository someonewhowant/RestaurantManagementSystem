import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-ui-modal',
  standalone: true,
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss'
})
export class UiModalComponent {
  title = input<string>('');
  isOpen = input<boolean>(false);
  onClose = output<void>();

  close() {
    this.onClose.emit();
  }
}
