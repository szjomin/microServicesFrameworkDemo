package com.jm.oauth.config;
import com.jm.oauth.util.UserJwt;
import com.jm.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            // 查询数据库 oathu_client_details
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
                /*return new User(
                        username,  // 客户单id
                        new BCryptPasswordEncoder().encode(clientSecret), //客户端密钥加密操作
                        AuthorityUtils.commaSeparatedStringToAuthorityList("")); //权限
                *///数据库查找方式
                return new User(
                        username, //客户单id
                        clientSecret, //客户端密钥->加密操作
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }

        if (StringUtils.isEmpty(username)) {
            return null;
        }

        // 从数据库加载查询用户信息
        Result<com.jm.user.pojo.User> userResult = userFeign.findById(username);

        //客户端 Id  ：
        //客户端 密钥 ：

        if(userResult==null || userResult.getData()==null){
            return null;
        }


        //根据用户名查询用户信息
        //String pwd = new BCryptPasswordEncoder().encode("testpwd");
        String pwd = userResult.getData().getPassword();
        //创建User对象 String permissions = "user_list, goods_list,seckill_list";
        String permissions = "user,seckill_list";

        UserJwt userDetails = new UserJwt(username,pwd,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));


        //userDetails.setComy(songsi);
        return userDetails;
    }
}
