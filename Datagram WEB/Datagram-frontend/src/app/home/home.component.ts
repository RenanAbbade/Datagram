import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { UsuarioServiceService } from './../services/usuario-service.service';
import { PerfilComponent } from './../perfil/perfil.component';
import { PostServiceService } from './../services/post-service.service';
import { CadastroComponent } from './../cadastro/cadastro.component';
import { Component, OnInit } from '@angular/core';
import { NodeWithI18n } from '@angular/compiler';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  // tslint:disable-next-line: max-line-length
  constructor(private postService: PostServiceService, private usuarioService: UsuarioServiceService,  private UsuarioLogadoService: UsuarioLogadoService, private router: Router) { }

  // tslint:disable-next-line: max-line-length
  postagem = {titulo: '', subtitulo: '', conteudo: '', date: '', url: null, arquivoPublicacao: null, tipoPostagem: 'simples', palavrasChave: []};
  palavraChaveEscolhida = '';
  palavrasChaveList: Array<string> =  [];
  usuario = {nome: ''};
  rotaPesquisa = 'perfil-amigo';

  listaRetornoPesquisa;

  notificacoes;

  usuarioLogado;

  interessesPossiveis: Array<string> = ['algorithm', 'AngularJS', 'artificial intelligence', 'backpropagation', 'Bayes Thorem', 'Bayesian network', 'bias', 'Big Data', 'binomial distribution', 'chi-square test', 'classification', 'clustering', 'coefficient', 'computational linguistics', 'confidence interval', 'continuous variable', 'correlation', 'covariance', 'cross-validation', 'D3', 'data engineer', 'data mining', 'data science', 'data structure', 'data wrangling', 'decision trees', 'deep learning', 'dependent variable', 'dimension reduction', 'discrete variable', 'econometrics', 'feature', 'feature engineering',  'GATE',  'gradient boosting', 'gradient descent',  'histogram',  'independent variable', 'JavaScript', 'k-means clustering', 'k-nearest neighbors', 'latent variable', 'lift', 'linear algebra', 'linear regression', 'logarithm', 'logistic regression', 'machine learning', 'machine learning model', 'Markov Chain', 'MATLAB', 'matrix', 'mean', 'Mean Absolute Error', 'Mean Squared Error', 'median', 'mode', 'model', 'Monte Carlo method', 'moving average', 'n-gram', 'naive Bayes classifier', 'neural network', 'normal distribution', 'NoSQL', 'null hypothesis', 'objective function', 'outlier', 'overfitting', 'P value', 'PageRank', 'Pandas', 'perceptron', 'Perl', 'pivot table', 'Poisson distribution', 'posterior distribution', 'predictive analytics', 'predictive modeling', 'principal component analysis', 'prior distribution', 'probability distribution', 'Python', 'quantile, quartile', 'R', 'random forest', 'regression', 'reinforcement learning', 'Root Mean Squared Error', 'Ruby', 'S curve', 'SAS', 'scalar', 'scripting', 'serial correlation', 'shell', 'spatiotemporal data', 'SPSS', 'SQL', 'standard deviation', 'standard normal distribution', 'standardized score', 'Stata', 'strata, stratified sampling', 'supervised learning', 'support vector machine', 't-distribution', 'Tableau', 'time series data', 'UIMA', 'unsupervised learning', 'variance', 'vector', 'vector space', 'Weka'];

  public publicarPost(){


    const dataAtual = new Date();
    const dia = dataAtual.getDate();
    const mes = dataAtual.getMonth() + 1;
    const ano = dataAtual.getFullYear();
    const hora = dataAtual.getHours();
    const minuto = dataAtual.getMinutes();


    this.postagem.date = dia + '/' + mes + '/' + ano + ' ' + hora + ':' + minuto;

    this.postService.postPublicacao(this.postagem);

    window.location.reload();

  }

public getUsuarioByNome(nome){
  this.usuarioService.getUsuarioByNome(nome).subscribe(res => {
    this.listaRetornoPesquisa = JSON.parse(JSON.stringify(res));
  });
}

checkPage(){
  const rota = this.router.url;
  if (rota.startsWith('/feed/perfil-amigo') || rota.startsWith('/perfil/perfil-amigo')){
    this.router.navigate(['../feed']);
  }
}

getNotificacao(){
  this.postService.getNotificacoes().subscribe(res => {
    this.notificacoes = JSON.parse(JSON.stringify(res));
  });
}
public clearSessionStorage(){
  sessionStorage.clear();
}

getUsuarioLogado(){
  this.UsuarioLogadoService.getUsuarioLogado().subscribe(res => {
    this.usuarioLogado = JSON.parse(JSON.stringify(res));
    console.log(this.usuarioLogado);
  });
}

ngOnInit(): void {}

public mudaTipo(tipoPostagem){
  if (tipoPostagem === 'cientifica'){
    // Modificando aparencia do selecionado
    document.getElementById('Cientifica').className = 'btn btn-primary';
    document.getElementById('Simples').className = 'btn btn-secondary';

    // visibilizando elementos do pesquisador
    document.getElementById('arquivoPesquisador').style.display = 'inline';
  }else{
    // Modificando aparencia do selecionado
    document.getElementById('Simples').className = 'btn btn-primary';
    document.getElementById('Cientifica').className = 'btn btn-secondary';

    // escondendo elementos do pesquisador
    document.getElementById('arquivoPesquisador').style.display = 'none';
    }

  this.postagem.tipoPostagem = tipoPostagem;
  }

  uploadArquivo(e){
    var file = e.dataTransfer ? e.dataTransfer.files[0] : e.target.files[0];
    var reader = new FileReader();

    if (!file.type.match('pdf')) {
      alert('formato inválido, por favor, insira um documento .pdf');
      return;
    }

    reader.onload = this._handleReaderLoaded.bind(this);
    reader.readAsDataURL(file);
  }
  _handleReaderLoaded(e) {
    let reader = e.target;
    this.postagem.arquivoPublicacao  = reader.result;
  }

  escolheInteresse(){
    this.palavrasChaveList.push(this.palavraChaveEscolhida);
    // tslint:disable-next-line: max-line-length
    this.postagem.palavrasChave = this.palavrasChaveList.filter(x => x.trim().length > 0);//Por algum motivo na hora de salvar a lista, o angular salva um espaço em branco como elemento, esta linha tem o papel de tirar este elemento que é um espaco em branco
  }
}
