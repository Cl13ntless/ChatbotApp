import { Component, OnDestroy, OnInit } from '@angular/core';
import { NbIconLibraries } from '@nebular/theme';
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
  currentLat: any = 52.03096758574192;
  currentLon: any = 8.537116459846818;
  currentLocation: string = "Aktuelle Position: Bielefeld Herforderstraße 69 , Deutschland";
  languageSVG: string = "de";

  constructor(private iconLibraries: NbIconLibraries) {
    this.iconLibraries.getPack("eva").icons.set(
      "de", "<img src='./assets/de.svg' width='25px'/>"
    )
    this.iconLibraries.getPack("eva").icons.set(
      "gb", "<img src='./assets/gb.svg' width='25px'/>"
    )
  }

  ngOnInit() {
    this.addBotMessage('Human presence detected 🤖. Wie kann ich dir behilflich sein? ');
    this.webSocketAPI = new WebSocketAPI(this);
    this.webSocketAPI._connect();
    var ws = this.webSocketAPI;
    var component = this;
    setTimeout( function(){
      ws._sendLanguageChange(component.languageSVG);
    }, 800);

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
      sender: 'Julius Figge ',
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
    window.scroll(0,document.documentElement.scrollHeight);
  }

  addWeatherImage(toDisplay?: any): void{

    var files = [ { url: `/assets/${toDisplay}.png`, type: 'image/png' } ];
    this.messages.push({
      avatar: '/assets/bot.jpeg',
      type: 'file',
      files: files,
    });
    setTimeout( function(){
      window.scroll(0,document.documentElement.scrollHeight);
    }, 50);

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

  changeLanguage(){
    if(this.languageSVG == "de"){
      this.languageSVG = "gb";
    } else {
      this.languageSVG = "de";
    }
    this.webSocketAPI._sendLanguageChange(this.languageSVG);
  }
}
