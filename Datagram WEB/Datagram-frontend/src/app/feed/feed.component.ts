import { PostServiceService } from './../services/post-service.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.css']
})
export class FeedComponent implements OnInit {

  constructor(private postService: PostServiceService) { }

  postagens;

  buildFeedUsuarioLogado(){
    this.postService.buildFeed().subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      console.log(this.postagens);
    });
  }

  ngOnInit(): void {
    this.buildFeedUsuarioLogado();
  }

}
