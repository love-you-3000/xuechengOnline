package com.xuecheng.media.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.entity.MediaProcess;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    @Select("SELECT * FROM media_process WHERE id%#{shardTotal}=#{shardIndex} AND (`status`=1 OR `status`=3) AND fail_count<3 LIMIT #{count}")
    List<MediaProcess> selectByShardIndex(int shardTotal, int shardIndex, int count);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("UPDATE media_process m SET m.status='4' WHERE (m.status='1' OR m.status='3') AND m.fail_count<3 AND m.id=#{id}")
    int startTask(long id);

}
