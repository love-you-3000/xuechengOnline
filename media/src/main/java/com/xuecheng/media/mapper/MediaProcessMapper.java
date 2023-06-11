package com.xuecheng.media.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.entity.MediaProcess;
import org.apache.ibatis.annotations.Select;

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
}
