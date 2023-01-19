import { Component, OnDestroy, OnInit } from '@angular/core';
import { RxStompService } from '../rx-stomp.service';
import { Message } from '@stomp/stompjs'
import { Subscription } from 'rxjs'

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit, OnDestroy{
  receivedMessages: string[] = [];
  message: string = "";
  // @ts-ignore
  private topicSubscription: Subscription;

  constructor(private rxStompService: RxStompService) {}

  ngOnInit(): void {
    this.topicSubscription = this.rxStompService.watch('/topic/weather').subscribe((message: Message) => {
      console.log(JSON.parse(message.body).content);
      console.log(this.receivedMessages[0]);
      this.receivedMessages.push(JSON.parse(message.body).content);
      console.log(this.receivedMessages);
    });
  }

  ngOnDestroy(): void {
    this.topicSubscription.unsubscribe();
  }

  onSendMessage() {
    const message = this.message;
    this.rxStompService.publish({ destination: '/app/inquiry',
     body: JSON.stringify(message),
     headers:{'content-type': 'application/json'}
    });
  }
}
