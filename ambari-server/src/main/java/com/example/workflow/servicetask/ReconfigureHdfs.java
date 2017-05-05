package com.example.workflow.servicetask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class ReconfigureHdfs extends AsyncServiceTask {
  private final static Random random = new Random();

  public void execute(ActivityExecution context) {
    LOG.info("Reconfiguring Hdfs activitId:" + context.getId());
    String myserviceid = serviceId(context);
    String newNameNodeHost = hosts(context).newNameNodeHost;
    String oldNameNodeHost = hosts(context).currentNameNodeHost;

    Map<String, String> hdfsSite = new HashMap<String,String>() {{
      put("Clusters/cluster_name", "cc"); // TODO
      put("Clusters/desired_config/type", "hdfs-site");
      put("Clusters/desired_config/tag", "version" + random.nextInt());
      put("Clusters/desired_config/properties/dfs.replication", "3");
      put("Clusters/desired_config/properties/dfs.namenode.audit.log.async", "true");
      put("Clusters/desired_config/properties/dfs.nameservices", serviceId(context));
      put("Clusters/desired_config/properties/dfs.namenode.checkpoint.dir", "/hadoop/hdfs/namesecondary");
      put("Clusters/desired_config/properties/dfs.namenode.http-address." + myserviceid + ".nn1", oldNameNodeHost + ":50070");
      put("Clusters/desired_config/properties/dfs.namenode.http-address." + myserviceid + ".nn2", newNameNodeHost + ":50070");
      put("Clusters/desired_config/properties/dfs.namenode.avoid.read.stale.datanode", "true");
      put("Clusters/desired_config/properties/dfs.journalnode.http-address", "0.0.0.0:8480");
      put("Clusters/desired_config/properties/nfs.file.dump.dir", "/tmp/.hdfs-nfs");
      put("Clusters/desired_config/properties/dfs.namenode.rpc-address." + myserviceid + ".nn2", newNameNodeHost + ":8020");
      put("Clusters/desired_config/properties/dfs.namenode.rpc-address." + myserviceid + ".nn1", oldNameNodeHost + ":8020");
      put("Clusters/desired_config/properties/dfs.internal.nameservices", myserviceid);
      put("Clusters/desired_config/properties/dfs.encrypt.data.transfer.cipher.suites", "AES/CTR/NoPadding");
      put("Clusters/desired_config/properties/dfs.client.read.shortcircuit.streams.cache.size", "4096");
      put("Clusters/desired_config/properties/dfs.hosts.exclude", "/etc/hadoop/conf/dfs.exclude");
      put("Clusters/desired_config/properties/dfs.client.failover.proxy.provider." + myserviceid, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
      put("Clusters/desired_config/properties/dfs.namenode.accesstime.precision", "0");
      put("Clusters/desired_config/properties/dfs.namenode.fslock.fair", "false");
      put("Clusters/desired_config/properties/dfs.permissions.enabled", "true");
      put("Clusters/desired_config/properties/dfs.datanode.balance.bandwidthPerSec", "6250000");
      put("Clusters/desired_config/properties/dfs.namenode.stale.datanode.interval", "30000");
      put("Clusters/desired_config/properties/dfs.content-summary.limit", "5000");
      put("Clusters/desired_config/properties/dfs.http.policy", "HTTP_ONLY");
      put("Clusters/desired_config/properties/dfs.ha.fencing.methods", "shell(/bin/true)");
      put("Clusters/desired_config/properties/dfs.journalnode.https-address", "0.0.0.0:8481");
      put("Clusters/desired_config/properties/dfs.datanode.du.reserved", "65660336128");
      put("Clusters/desired_config/properties/dfs.domain.socket.path", "/var/lib/hadoop-hdfs/dn_socket");
      put("Clusters/desired_config/properties/dfs.datanode.ipc.address", "0.0.0.0:8010");
      put("Clusters/desired_config/properties/dfs.cluster.administrators", "hdfs");
      put("Clusters/desired_config/properties/dfs.datanode.max.transfer.threads", "4096");
      put("Clusters/desired_config/properties/dfs.namenode.handler.count", "25");
      put("Clusters/desired_config/properties/dfs.https.port", "50470");
      put("Clusters/desired_config/properties/dfs.replication.max", "50");
      put("Clusters/desired_config/properties/dfs.client.read.shortcircuit", "true");
      put("Clusters/desired_config/properties/dfs.webhdfs.enabled", "true");
      put("Clusters/desired_config/properties/dfs.namenode.name.dir", "/hadoop/hdfs/namenode");
      put("Clusters/desired_config/properties/dfs.namenode.avoid.write.stale.datanode", "true");
      put("Clusters/desired_config/properties/dfs.datanode.https.address", "0.0.0.0:50475");
      put("Clusters/desired_config/properties/dfs.datanode.failed.volumes.tolerated", "0");
      put("Clusters/desired_config/properties/dfs.namenode.https-address." + myserviceid + ".nn2", newNameNodeHost + ":50470");
      put("Clusters/desired_config/properties/dfs.namenode.https-address." + myserviceid + ".nn1", oldNameNodeHost + ":50470");
      put("Clusters/desired_config/properties/dfs.client.retry.policy.enabled", "false");
      put("Clusters/desired_config/properties/hadoop.caller.context.enabled", "true");
      put("Clusters/desired_config/properties/dfs.namenode.startup.delay.block.deletion.sec", "3600");
      put("Clusters/desired_config/properties/dfs.block.access.token.enable", "true");
      put("Clusters/desired_config/properties/dfs.datanode.data.dir", "/hadoop/hdfs/data");
      put("Clusters/desired_config/properties/dfs.permissions.superusergroup", "hdfs");
      put("Clusters/desired_config/properties/dfs.blocksize", "134217728");
      put("Clusters/desired_config/properties/dfs.namenode.shared.edits.dir", "qjournal://" + newNameNodeHost + ":8485;" + oldNameNodeHost + ":8485;c6403.ambari.apache.org:8485/" + myserviceid);
      put("Clusters/desired_config/properties/dfs.namenode.checkpoint.edits.dir", "${dfs.namenode.checkpoint.dir}");
      put("Clusters/desired_config/properties/nfs.exports.allowed.hosts", "* rw");
      put("Clusters/desired_config/properties/dfs.datanode.address", "0.0.0.0:50010");
      put("Clusters/desired_config/properties/dfs.blockreport.initialDelay", "120");
      put("Clusters/desired_config/properties/dfs.datanode.data.dir.perm", "750");
      put("Clusters/desired_config/properties/dfs.namenode.write.stale.datanode.ratio", "1.0f");
      put("Clusters/desired_config/properties/dfs.namenode.name.dir.restore", "true");
      put("Clusters/desired_config/properties/dfs.heartbeat.interval", "3");
      put("Clusters/desired_config/properties/dfs.namenode.checkpoint.txns", "1000000");
      put("Clusters/desired_config/properties/dfs.journalnode.edits.dir", "/hadoop/hdfs/journal");
      put("Clusters/desired_config/properties/dfs.support.append", "true");
      put("Clusters/desired_config/properties/fs.permissions.umask-mode", "022");
      put("Clusters/desired_config/properties/dfs.namenode.safemode.threshold-pct", "0.99f");
      put("Clusters/desired_config/properties/dfs.namenode.checkpoint.period", "21600");
      put("Clusters/desired_config/properties/dfs.datanode.http.address", "0.0.0.0:50075");
      put("Clusters/desired_config/properties/dfs.ha.namenodes." + myserviceid, "nn1,nn2");
      put("Clusters/desired_config/properties/dfs.ha.automatic-failover.enabled", "true");
    }};
    api.modifyConfig(hdfsSite);

    Map coreSite = new HashMap<String, String>() {{
      put("Clusters/cluster_name", "cc"); // TODO
      put("Clusters/desired_config/type", "core-site");
      put("Clusters/desired_config/tag", "version" + random.nextInt());
      put("Clusters/desired_config/properties/fs.defaultFS", "hdfs://" + myserviceid);
      put("Clusters/desired_config/properties/ha.failover-controller.active-standby-elector.zk.op.retries", "120");
      put("Clusters/desired_config/properties/hadoop.security.authentication", "simple");
      put("Clusters/desired_config/properties/ipc.server.tcpnodelay", "true");
      put("Clusters/desired_config/properties/hadoop.proxyuser.hdfs.hosts", "*");
      put("Clusters/desired_config/properties/mapreduce.jobtracker.webinterface.trusted", "false");
      put("Clusters/desired_config/properties/hadoop.security.auth_to_local", "DEFAULT");
      put("Clusters/desired_config/properties/ipc.client.connect.max.retries", "50");
      put("Clusters/desired_config/properties/hadoop.proxyuser.root.groups", "*");
      put("Clusters/desired_config/properties/io.file.buffer.size", "131072");
      put("Clusters/desired_config/properties/ipc.client.idlethreshold", "8000");
      put("Clusters/desired_config/properties/hadoop.proxyuser.hdfs.groups", "*");
      put("Clusters/desired_config/properties/fs.trash.interval", "360");
      put("Clusters/desired_config/properties/hadoop.http.authentication.simple.anonymous.allowed", "true");
      put("Clusters/desired_config/properties/hadoop.security.authorization", "false");
      put("Clusters/desired_config/properties/net.topology.script.file.name", "/etc/hadoop/conf/topology_script.py");
      put("Clusters/desired_config/properties/io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.SnappyCodec");
      put("Clusters/desired_config/properties/ipc.client.connection.maxidletime", "30000");
      put("Clusters/desired_config/properties/ha.zookeeper.quorum", newNameNodeHost + ":2181,c6403.ambari.apache.org:2181," + oldNameNodeHost + ":2181");
      put("Clusters/desired_config/properties/hadoop.proxyuser.root.hosts", oldNameNodeHost);
      put("Clusters/desired_config/properties/io.serializations", "org.apache.hadoop.io.serializer.WritableSerialization");
    }};
    api.modifyConfig(coreSite);

    Long id = api.installComponent(hosts(context).newNameNodeHost, "HDFS_CLIENT");
    pendingTasks.registerCommand(context.getId(), Arrays.asList(id));
  }
}
