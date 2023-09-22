getPosts
===
    select
    -- @pageTag(){
    *
    -- @}
    from bbs_post where true
    -- @if(!isEmpty(topicId)){
    	 and `topic_id`=#{topicId}
    -- @}
    -- @if(!isEmpty(isAdmin)){
         order by id desc
    -- @}
  

deleteByTopicId
===
    delete from bbs_post where `topic_id`=#{topicId}


	
