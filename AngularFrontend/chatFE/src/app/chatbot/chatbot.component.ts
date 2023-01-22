import { Component, OnDestroy, OnInit } from '@angular/core';
import { WebSocketAPI } from '../WebSocketAPI';


const dialogflowURL = 'https://YOUR-CLOUDFUNCTION/dialogflowGateway';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit, OnDestroy{
  //@ts-ignore
  webSocketAPI: WebSocketAPI;
  messages: any = [];
  loading = false;

  constructor() {}

  ngOnInit() {
    this.addBotMessage('Human presence detected 🤖. Wie kann ich dir behilflich sein? ');
    this.webSocketAPI = new WebSocketAPI(this);
    this.webSocketAPI._connect();
  
  }

  handleUserMessage(event: any) {
    console.log(event);
    const text = event.message;
    this.addUserMessage(text);
    this.loading = true;
    this.webSocketAPI._send(text);
  }

  addUserMessage(text: string) {
    this.messages.push({
      text,
      sender: 'You',
      avatar: '/assets/bot.jpeg',
      reply: true,
      date: new Date()
    });
  }

  addBotMessage(text: string) {
    this.messages.push({
      text,
      sender: 'Bot',
      avatar: '/assets/bot.jpeg',
      date: new Date()
    });
  }

  handleMessage(message:any){
    this.addBotMessage(message);
      this.loading = false;

  }

  ngOnDestroy(): void {
    this.webSocketAPI._disconnect();
  }

}