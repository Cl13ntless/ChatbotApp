import { Component, OnDestroy, OnInit } from '@angular/core';
import { NbThemeService } from '@nebular/theme';
import { WebSocketAPI } from '../WebSocketAPI';

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
  currentLat: any;
  currentLon: any;
  currentLocation: string = "Aktuelle Position: Bielefeld Herforderstraße 69 , Deutschland";

  constructor() {}

  ngOnInit() {
    this.addBotMessage('Human presence detected 🤖. Wie kann ich dir behilflich sein? ');
    this.webSocketAPI = new WebSocketAPI(this);
    this.webSocketAPI._connect();
  }

  handleUserMessage(event: any): void {
    console.log(event);
    const text = event.message;
    this.addUserMessage(text);
    this.loading = true;
    this.webSocketAPI._send(text);
  }

  addUserMessage(text: string): void {
    this.messages.push({
      text,
      sender: 'Julius Figge',
      avatar: '/assets/bot.jpeg',
      reply: true,
      date: new Date()
    });
  }

  addBotMessage(text: string): void {
    this.messages.push({
      text,
      sender: 'Bot',
      avatar: '/assets/bot.jpeg',
      date: new Date()
    });
    window.scroll(0,document.body.scrollHeight);
  }

  addWeatherImage(toDisplay?: any): void{

    var files = [ { url: `/assets/${toDisplay}.png`, type: 'image/png' } ];
    this.messages.push({
      avatar: '/assets/bot.jpeg',
      type: 'file',
      files: files,
    });
    window.scroll(0,document.body.scrollHeight);
  }

  handleMessage(message:any): void{
    this.addBotMessage(message);
      this.loading = false;

  }

  ngOnDestroy(): void {
    this.webSocketAPI._disconnect();
  }

  getLocation(): void{
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position)=>{
          this.currentLon = position.coords.longitude;
          this.currentLat = position.coords.latitude;
          console.log(this.currentLat.toString());
          console.log(this.currentLon.toString());
          this.webSocketAPI._sendLocation(this.currentLon,this.currentLat);
        });
    } else {
       console.log("No support for geolocation")
    }
  }

  updateCurrentLocation(location: string): void{
    this.currentLocation = location;
  }

}