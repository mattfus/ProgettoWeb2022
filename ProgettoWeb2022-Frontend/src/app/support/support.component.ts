import { Component } from '@angular/core';
import { ServerService } from '../server.service';

@Component({
  selector: 'app-support',
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.css']
})
export class SupportComponent {

  constructor(private service: ServerService){
  }

  onSendSupport(){
    let name = (<HTMLInputElement>document.getElementById('name')).value;
    let email = (<HTMLInputElement>document.getElementById('email')).value;
    let message = (<HTMLInputElement>document.getElementById('message')).value;
    let subject = (<HTMLInputElement>document.getElementById('subject')).value;

    this.service.sendSupport(name, email, message, subject).subscribe(result => {
      if(result){
        alert("Email inviata correttamente.");
      } else {
        alert("C'è stato un errore, riprova più tardi.");
      }
    });

    $('#name').val('');
    $('#email').val('');
    $('#message').val('');
    $('#subject').val('');

  }

}
