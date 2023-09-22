allReply
===
    select  * 
    from bbs_reply where true
    -- @if(!isEmpty(postId)){
    	 and `post_id`=#{postId}
    -- @}
    -- @if(!isEmpty(isAdmin)){
         order by id desc
    -- @}
    

deleteByTopicId
===
    delete from bbs_reply where `topic_id`=#{topicId}

deleteByPostId
===
    delete from bbs_reply where `post_id`=#{postId}
