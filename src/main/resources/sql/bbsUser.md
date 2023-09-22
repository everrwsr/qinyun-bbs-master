
getScoreTop
===

* 得到积分最高的前N个用户

	select * from bbs_user order by score desc  limit #{max}


getLeveTop
===

	select * from bbs_user order by level desc,score desc  limit #{max}
	


