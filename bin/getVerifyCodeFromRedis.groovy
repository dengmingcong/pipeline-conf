import redis.clients.jedis.Jedis;
import org.apache.commons.lang3.StringUtils;

def redisHost = props.get("cloud.middleware.redis.host");
def redisPort = Integer.parseInt(props.get("cloud.middleware.redis.port"));
def redisPassword = props.get("cloud.middleware.redis.password");
def email = Parameters

Jedis jedis = new Jedis(redisHost, redisPort);
jedis.auth(redisPassword);
jedis.select(0);
String code = jedis.get("w:a:${email}:${email}");
log.info("verify code got from redis: " + code);
vars.put("code",code);