package com.alibaba.datax.plugin.reader.hanareader;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.reader.CommonRdbmsReader;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;

import java.util.List;

public class HanaReader extends Reader {

    private static final DataBaseType DATABASE_TYPE = DataBaseType.HANA;

    public static class Job extends Reader.Job {
        private Configuration jobConfig = null;
        private CommonRdbmsReader.Job commonRdbmsReaderMaster;

        @Override
        public void init() {
            this.jobConfig = super.getPluginJobConf();
            int fetchSize = this.jobConfig.getInt(com.alibaba.datax.plugin.rdbms.reader.Constant.FETCH_SIZE,
                    Constant.DEFAULT_FETCH_SIZE);
            if (fetchSize < 1) {
                throw DataXException.asDataXException(DBUtilErrorCode.REQUIRED_VALUE,
                        String.format("您配置的fetchSize有误，根据DataX的设计，fetchSize : [%d] 设置值不能小于 1.", fetchSize));
            }
            this.jobConfig.set(com.alibaba.datax.plugin.rdbms.reader.Constant.FETCH_SIZE, fetchSize);

            this.commonRdbmsReaderMaster = new CommonRdbmsReader.Job(DATABASE_TYPE);
            this.commonRdbmsReaderMaster.init(this.jobConfig);
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            return this.commonRdbmsReaderMaster.split(this.jobConfig, mandatoryNumber);
        }

        @Override
        public void post() {
            this.commonRdbmsReaderMaster.post(this.jobConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsReaderMaster.destroy(this.jobConfig);
        }
    }

    public static class Task extends Reader.Task {

        private Configuration jobConfig;
        private CommonRdbmsReader.Task commonRdbmsReaderSlave;

        @Override
        public void init() {
            this.jobConfig = super.getPluginJobConf();
            this.commonRdbmsReaderSlave = new CommonRdbmsReader.Task(DATABASE_TYPE, super.getTaskGroupId(), super.getTaskId());
            this.commonRdbmsReaderSlave.init(this.jobConfig);
        }

        @Override
        public void startRead(RecordSender recordSender) {
            int fetchSize = this.jobConfig.getInt(com.alibaba.datax.plugin.rdbms.reader.Constant.FETCH_SIZE, Constant.DEFAULT_FETCH_SIZE);

            this.commonRdbmsReaderSlave.startRead(this.jobConfig, recordSender, super.getTaskPluginCollector(), fetchSize);
        }

        @Override
        public void post() {
            this.commonRdbmsReaderSlave.post(this.jobConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsReaderSlave.destroy(this.jobConfig);
        }
    }
}
