import redis.clients.jedis.Jedis;
import org.apache.commons.lang3.StringUtils;

def redisHost = props.get("cloud.middleware.redis.host");
def redisPort = Integer.parseInt(props.get("cloud.middleware.redis.port"));
def redisPassword = props.get("cloud.middleware.redis.password");
def time = Parameters

Jedis jedis = new Jedis(redisHost, redisPort);
jedis.auth(redisPassword);
jedis.select(0);
String image_code = jedis.get("w:u:captcha:192_168_104_54_${time}");
log.info("verify image_code got from redis: " + image_code);
vars.put("image_code",image_code);