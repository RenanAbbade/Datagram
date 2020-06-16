import { PostServiceService } from './../services/post-service.service';
import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuarioServiceService } from '../services/usuario-service.service';
import { HttpClient } from '@angular/common/http';
import { AppConstants } from '../app-constants';


@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {

  usuario;

  id;

  postagens;

  postagem = {id: Number, autor: '', titulo: '', subtitulo: '', conteudo: '', date: '', curtida: 0, idsCurtida: []};

  UFS: Array<string> = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA', 'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'];

  // tslint:disable-next-line: max-line-length
  Municipios: Array<string> = [];

  fotoPerfil = '';

  interessesPossiveis: Array<string> = ['algorithm', 'AngularJS', 'artificial intelligence', 'backpropagation', 'Bayes Thorem', 'Bayesian network', 'bias', 'Big Data', 'binomial distribution', 'chi-square test', 'classification', 'clustering', 'coefficient', 'computational linguistics', 'confidence interval', 'continuous variable', 'correlation', 'covariance', 'cross-validation', 'D3', 'data engineer', 'data mining', 'data science', 'data structure', 'data wrangling', 'decision trees', 'deep learning', 'dependent variable', 'dimension reduction', 'discrete variable', 'econometrics', 'feature', 'feature engineering',  'GATE',  'gradient boosting', 'gradient descent',  'histogram',  'independent variable', 'JavaScript', 'k-means clustering', 'k-nearest neighbors', 'latent variable', 'lift', 'linear algebra', 'linear regression', 'logarithm', 'logistic regression', 'machine learning', 'machine learning model', 'Markov Chain', 'MATLAB', 'matrix', 'mean', 'Mean Absolute Error', 'Mean Squared Error', 'median', 'mode', 'model', 'Monte Carlo method', 'moving average', 'n-gram', 'naive Bayes classifier', 'neural network', 'normal distribution', 'NoSQL', 'null hypothesis', 'objective function', 'outlier', 'overfitting', 'P value', 'PageRank', 'Pandas', 'perceptron', 'Perl', 'pivot table', 'Poisson distribution', 'posterior distribution', 'predictive analytics', 'predictive modeling', 'principal component analysis', 'prior distribution', 'probability distribution', 'Python', 'quantile, quartile', 'R', 'random forest', 'regression', 'reinforcement learning', 'Root Mean Squared Error', 'Ruby', 'S curve', 'SAS', 'scalar', 'scripting', 'serial correlation', 'shell', 'spatiotemporal data', 'SPSS', 'SQL', 'standard deviation', 'standard normal distribution', 'standardized score', 'Stata', 'strata, stratified sampling', 'supervised learning', 'support vector machine', 't-distribution', 'Tableau', 'time series data', 'UIMA', 'unsupervised learning', 'variance', 'vector', 'vector space', 'Weka'];

  interesseEscolhido = '';

  interessesEscolhidos = '';

  seguidoresUsuario;

  usuarioSeguindo;

  // tslint:disable-next-line: max-line-length
  constructor(private usuarioService: UsuarioServiceService, private usuarioLogadoService: UsuarioLogadoService, private postService: PostServiceService, private route: ActivatedRoute, private router: Router, private http: HttpClient) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
   }



  public updateUsuario(){

    this.usuarioService.updateUsuario(this.usuario).subscribe(res => {
      this.usuario = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
    });
  }

  public initMunicipio(){

    if (this.usuario.estado != null){
      this.http.post(AppConstants.consultaMunicipio, this.usuario.estado).subscribe(data => {

          this.Municipios = JSON.parse(JSON.stringify(data));
      });
    }
  }


  public editPost(id, act){
    for (let post of this.postagens) {
      if (post.id === id){
        this.postagem.id = post.id;
        this.postagem.titulo = post.titulo;
        this.postagem.subtitulo = post.subtitulo;
        this.postagem.conteudo = post.conteudo;
        this.postagem.curtida = post.curtida;
        this.postagem.autor = post.autor;
        this.postagem.date = post.date;
      }
     }
    if (act === 'del'){
       this.deletePost();
     }

    if (act === 'like'){

       this.postagem.idsCurtida.push(this.usuario.id);
       this.postagem.curtida++;
       this.updatePost();
    }
  }


  public updatePost(){
    this.postService.updatePost(this.postagem).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      this.ngOnInit(); //Reload sem precisar recarregar a pagina
    });
  }

  public deletePost(){
    this.postService.deletePost(this.postagem.id).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
    });
  }


  public visualizarImg(e){
    var file = e.dataTransfer ? e.dataTransfer.files[0] : e.target.files[0];
    var pattern = /image-*/;
    var reader = new FileReader();
    if (!file.type.match(pattern)) {
      alert('invalid format');
      return;
    }
    reader.onload = this._handleReaderLoaded.bind(this);
    reader.readAsDataURL(file);
  }
  _handleReaderLoaded(e) {
    let reader = e.target;
    this.usuario.fotoPerfil  = reader.result;
    this.updateUsuario();
  }

  escolheInteresse(){
    this.usuario.interesses.push(this.interesseEscolhido);
    // tslint:disable-next-line: max-line-length
    this.usuario.interesses = this.usuario.interesses.filter(x => x.trim().length > 0);//Por algum motivo na hora de salvar a lista, o angular salva um espaço em branco como elemento, esta linha tem o papel de tirar este elemento que é um espaco em branco
    this.updateUsuario();
  }

  mostraSeguidores(){
    this.usuarioService.getSeguidores().subscribe(res => {
      this.seguidoresUsuario = JSON.parse(JSON.stringify(res));
      console.log(this.seguidoresUsuario);
    });
  }

  mostraSeguindo(){
    this.usuarioService.getSeguindo().subscribe(res => {
      this.usuarioSeguindo = JSON.parse(JSON.stringify(res));
      console.log(this.usuarioSeguindo);
    });
  }

ngOnInit(): void {

    this.usuario = this.usuarioLogadoService.getUsuarioLogado().subscribe(data => {
    this.usuario = JSON.parse(JSON.stringify(data));

    this.usuarioService.getPostsUsuario(this.usuario.id).subscribe(res => {
    this.postagens = JSON.parse(JSON.stringify(res));
    console.log(this.postagens);
      });
    });
  }
}

