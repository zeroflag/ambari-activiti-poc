package com.example.workflow.servicetask;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;

import com.google.gson.Gson;

public class ReconfigureHdfs extends ServerTask {
  private static final Gson gson = new Gson();

  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Reconfiguring Hdfs");
    client.modifyConfiguration(
      "hdfs-site",
       gson.fromJson(("{\n" +
        "          \"dfs.block.access.token.enable\": \"true\",\n" +
        "          \"dfs.blockreport.initialDelay\": \"120\",\n" +
        "          \"dfs.blocksize\": \"134217728\",\n" +
        "          \"dfs.client.read.shortcircuit\": \"true\",\n" +
        "          \"dfs.client.read.shortcircuit.streams.cache.size\": \"4096\",\n" +
        "          \"dfs.client.retry.policy.enabled\": \"false\",\n" +
        "          \"dfs.cluster.administrators\": \" hdfs\",\n" +
        "          \"dfs.content-summary.limit\": \"5000\",\n" +
        "          \"dfs.datanode.address\": \"0.0.0.0:50010\",\n" +
        "          \"dfs.datanode.balance.bandwidthPerSec\": \"6250000\",\n" +
        "          \"dfs.datanode.data.dir\": \"\\/hadoop\\/hdfs\\/data\",\n" +
        "          \"dfs.datanode.data.dir.perm\": \"750\",\n" +
        "          \"dfs.datanode.du.reserved\": \"65660336128\",\n" +
        "          \"dfs.datanode.failed.volumes.tolerated\": \"0\",\n" +
        "          \"dfs.datanode.http.address\": \"0.0.0.0:50075\",\n" +
        "          \"dfs.datanode.https.address\": \"0.0.0.0:50475\",\n" +
        "          \"dfs.datanode.ipc.address\": \"0.0.0.0:8010\",\n" +
        "          \"dfs.datanode.max.transfer.threads\": \"4096\",\n" +
        "          \"dfs.domain.socket.path\": \"\\/var\\/lib\\/hadoop-hdfs\\/dn_socket\",\n" +
        "          \"dfs.encrypt.data.transfer.cipher.suites\": \"AES\\/CTR\\/NoPadding\",\n" +
        "          \"dfs.heartbeat.interval\": \"3\",\n" +
        "          \"dfs.hosts.exclude\": \"\\/etc\\/hadoop\\/conf\\/dfs.exclude\",\n" +
        "          \"dfs.http.policy\": \"HTTP_ONLY\",\n" +
        "          \"dfs.https.port\": \"50470\",\n" +
        "          \"dfs.journalnode.edits.dir\": \"\\/hadoop\\/hdfs\\/journal\",\n" +
        "          \"dfs.journalnode.http-address\": \"0.0.0.0:8480\",\n" +
        "          \"dfs.journalnode.https-address\": \"0.0.0.0:8481\",\n" +
        "          \"dfs.namenode.accesstime.precision\": \"0\",\n" +
        "          \"dfs.namenode.audit.log.async\": \"true\",\n" +
        "          \"dfs.namenode.avoid.read.stale.datanode\": \"true\",\n" +
        "          \"dfs.namenode.avoid.write.stale.datanode\": \"true\",\n" +
        "          \"dfs.namenode.checkpoint.dir\": \"\\/hadoop\\/hdfs\\/namesecondary\",\n" +
        "          \"dfs.namenode.checkpoint.edits.dir\": \"${dfs.namenode.checkpoint.dir}\",\n" +
        "          \"dfs.namenode.checkpoint.period\": \"21600\",\n" +
        "          \"dfs.namenode.checkpoint.txns\": \"1000000\",\n" +
        "          \"dfs.namenode.fslock.fair\": \"false\",\n" +
        "          \"dfs.namenode.handler.count\": \"25\",\n" +
        "          \"dfs.namenode.name.dir\": \"\\/hadoop\\/hdfs\\/namenode\",\n" +
        "          \"dfs.namenode.name.dir.restore\": \"true\",\n" +
        "          \"dfs.namenode.safemode.threshold-pct\": \"0.99f\",\n" +
        "          \"dfs.namenode.stale.datanode.interval\": \"30000\",\n" +
        "          \"dfs.namenode.startup.delay.block.deletion.sec\": \"3600\",\n" +
        "          \"dfs.namenode.write.stale.datanode.ratio\": \"1.0f\",\n" +
        "          \"dfs.permissions.enabled\": \"true\",\n" +
        "          \"dfs.permissions.superusergroup\": \"hdfs\",\n" +
        "          \"dfs.replication\": \"3\",\n" +
        "          \"dfs.replication.max\": \"50\",\n" +
        "          \"dfs.support.append\": \"true\",\n" +
        "          \"dfs.webhdfs.enabled\": \"true\",\n" +
        "          \"fs.permissions.umask-mode\": \"022\",\n" +
        "          \"hadoop.caller.context.enabled\": \"true\",\n" +
        "          \"nfs.exports.allowed.hosts\": \"* rw\",\n" +
        "          \"nfs.file.dump.dir\": \"\\/tmp\\/.hdfs-nfs\",\n" +
        "          \"dfs.nameservices\": \"myserviceid\",\n" +
        "          \"dfs.internal.nameservices\": \"myserviceid\",\n" +
        "          \"dfs.ha.namenodes.myserviceid\": \"nn1,nn2\",\n" +
        "          \"dfs.namenode.rpc-address.myserviceid.nn1\": \"__OLDNNHOST__:8020\",\n" +
        "          \"dfs.namenode.rpc-address.myserviceid.nn2\": \"__NEWNNHOST__:8020\",\n" +
        "          \"dfs.namenode.http-address.myserviceid.nn1\": \"__OLDNNHOST__:50070\",\n" +
        "          \"dfs.namenode.http-address.myserviceid.nn2\": \"__NEWNNHOST__:50070\",\n" +
        "          \"dfs.namenode.https-address.myserviceid.nn1\": \"__OLDNNHOST__:50470\",\n" +
        "          \"dfs.namenode.https-address.myserviceid.nn2\": \"__NEWNNHOST__:50470\",\n" +
        "          \"dfs.client.failover.proxy.provider.myserviceid\": \"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider\",\n" +
        "          \"dfs.namenode.shared.edits.dir\": \"qjournal:\\/\\/__NEWNNHOST__:8485;__OLDNNHOST__:8485;c6403.ambari.apache.org:8485\\/myserviceid\",\n" +
        "          \"dfs.ha.fencing.methods\": \"shell(\\/bin\\/true)\",\n" +
        "          \"dfs.ha.automatic-failover.enabled\": true\n" +
        "      }")
          .replaceAll("myserviceid", serviceId(context))
          .replaceAll("__OLDNNHOST__", hosts(context).currentNameNodeHost)
          .replaceAll("__NEWNNHOST__", hosts(context).newNameNodeHost), Map.class)
    );
    client.modifyConfiguration(
      "core-site",
       gson.fromJson(("{\n" +
         "          \"fs.defaultFS\": \"hdfs:\\/\\/myserviceid\",\n" +
         "          \"fs.trash.interval\": \"360\",\n" +
         "          \"ha.failover-controller.active-standby-elector.zk.op.retries\": \"120\",\n" +
         "          \"hadoop.http.authentication.simple.anonymous.allowed\": \"true\",\n" +
         "          \"hadoop.proxyuser.hdfs.groups\": \"*\",\n" +
         "          \"hadoop.proxyuser.hdfs.hosts\": \"*\",\n" +
         "          \"hadoop.proxyuser.root.groups\": \"*\",\n" +
         "          \"hadoop.proxyuser.root.hosts\": \"__OLDNNHOST__\",\n" +
         "          \"hadoop.security.auth_to_local\": \"DEFAULT\",\n" +
         "          \"hadoop.security.authentication\": \"simple\",\n" +
         "          \"hadoop.security.authorization\": \"false\",\n" +
         "          \"io.compression.codecs\": \"org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.SnappyCodec\",\n" +
         "          \"io.file.buffer.size\": \"131072\",\n" +
         "          \"io.serializations\": \"org.apache.hadoop.io.serializer.WritableSerialization\",\n" +
         "          \"ipc.client.connect.max.retries\": \"50\",\n" +
         "          \"ipc.client.connection.maxidletime\": \"30000\",\n" +
         "          \"ipc.client.idlethreshold\": \"8000\",\n" +
         "          \"ipc.server.tcpnodelay\": \"true\",\n" +
         "          \"mapreduce.jobtracker.webinterface.trusted\": \"false\",\n" +
         "          \"net.topology.script.file.name\": \"\\/etc\\/hadoop\\/conf\\/topology_script.py\",\n" +
         "          \"ha.zookeeper.quorum\": \"__NEWNNHOST__:2181,c6403.ambari.apache.org:2181,__OLDNNHOST__:2181\"\n" +
         "        }")
           .replaceAll("myserviceid", serviceId(context))
           .replaceAll("__OLDNNHOST__", hosts(context).currentNameNodeHost)
           .replaceAll("__NEWNNHOST__", hosts(context).newNameNodeHost), Map.class)
    );
    installComponentBlocking(hosts(context).newNameNodeHost, "HDFS_CLIENT");
  }
}
