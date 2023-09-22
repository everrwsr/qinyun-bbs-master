@ if(query.totalPage!=1){ //query 是通过标签传入的参数，对应类PageQuery
@ var pageUrl=has(url)?url:pagePatternUrl(),pageUrlParameter=pageParameterUrl();
<nav>
  <ul class="pagination">
	@if(query.totalPage<=10){//小于10页直接全部输出
		@for(var i in range(1,query.totalPage+1)){
			<li class="${i==query.pageNumber?'active'}"><a href="${ctxPath}${pageUrl}${i}${pageUrlParameter}">${i}</a></li>
		@}
	@}else{
		@var maxdiff = query.totalPage-query.pageNumber;
		@var start = query.pageNumber<=4?1:maxdiff<=3?(query.totalPage - 7):(query.pageNumber-3);
		@var end = query.pageNumber<=4?7:maxdiff<=3?query.totalPage:(query.pageNumber+3);
		
		@if(start>1){
			<li><a href="${ctxPath}${pageUrl}1.html${pageUrlParameter}">首页</a></li>
			<li class="disabled"><a href="javascript:;">···</a></li>
		@}
		
		@for(var i in range(start,end+1)){
			<li class="${i==query.pageNumber?'active'}"><a href="${ctxPath}${pageUrl}${i}${pageUrlParameter}">${i}</a></li>
		@}
		
		@if(end<query.totalPage){
			<li class="disabled"><a href="javascript:;">···</a></li>
			<li><a href="${ctxPath}${pageUrl}${query.totalPage}${pageUrlParameter}">尾页</a></li>
		@}
	@}
  </ul>
</nav>
@}