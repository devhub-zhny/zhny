package zhny.devhub.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import zhny.devhub.device.entity.Device;
import zhny.devhub.device.entity.DeviceProperty;
import zhny.devhub.device.entity.vo.SwitchVo;
import zhny.devhub.device.mapper.DeviceMapper;
import zhny.devhub.device.service.DeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author essarai
 * @since 2024-03-11 01:29:23
 */
@Slf4j
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Override
    public SwitchVo open(Long id) {
        Device device = this.baseMapper.selectById(id);
        SwitchVo switchVo = new SwitchVo();
        if (device != null) {
            device.setDeviceStatus(!device.getDeviceStatus());
            this.baseMapper.updateById(device);
            //开关物理设备
            Boolean bind = device.isBinding();
            if (!bind) {
                log.info(id + "设备还没绑定");
            } else {
                List<Long> ids = new ArrayList<>();
                Device temp = searchByPhysicalID(device.getDevicePhysicalId());
                while(temp != null){
                    ids.add(device.getParentDeviceId());
                    temp = searchByPhysicalID(temp.getDevicePhysicalId());
                }
                //TODO 此处操作具体物理设备
                switchVo.setDeviceType(device.getDeviceCategoryName());
            }
            log.info(id + "开关操作成功");
        } else {
            log.info(id + "设备不存在");
        }
        return switchVo;
    }

    @Override
    public Page<Device> all(int current, int pageSize) {
        return this.baseMapper.selectPage(
                new Page<>(current, pageSize),
                new QueryWrapper<>());
    }

    @Override
    public void bind(Long id) {
        Device device = this.baseMapper.selectById(id);
        if (device != null) {
            device.setBinding(true);
            this.baseMapper.updateById(device);
            log.info(device.getDeviceName() + "设备绑定成功");
        } else {
            log.info(id + "设备不存在");
        }

    }

    @Override
    public Page<Device> searchByStatus(int status, int current, int pageSize) {
        Page<Device> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<Device> queryWrapper = Wrappers.lambdaQuery(Device.class)
                .eq(status != 0, Device::getDeviceStatus, status);
        return this.baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Device searchByPhysicalID(Long physicalId){
        Device device = this.baseMapper.selectOne(
                Wrappers.lambdaQuery(Device.class)
                        .eq(Device::getDevicePhysicalId,physicalId)
        );
        return device;
    }


}
