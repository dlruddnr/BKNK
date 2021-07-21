package com.koreait.bknk.user;

import com.koreait.bknk.common.EmailService;
import com.koreait.bknk.common.MyConst;
import com.koreait.bknk.common.MyFileUtils;
import com.koreait.bknk.common.MySecurityUtils;
import com.koreait.bknk.security.IAuthenticationFacade;
import com.koreait.bknk.security.UserDetailsServiceImpl;
import com.koreait.bknk.user.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    @Autowired private UserMapper mapper;
    @Autowired private EmailService email; //메일보내기
    @Autowired private MySecurityUtils secUtils; //인증번호 얻기
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserDetailsServiceImpl userDetailService;
    @Autowired private MyConst myConst;
    @Autowired private IAuthenticationFacade auth;
    @Autowired private MyFileUtils myFileUtils;

    public void join(UserEntity param) {
        String authCd = secUtils.getRandomDigit(5);
        param.setAuthCd(authCd);

        String hashedPw = passwordEncoder.encode(param.getPw());
        param.setPw(hashedPw);

        param.setProvider(myConst.LOCAL);

        int result = userDetailService.join(param);

        if(result==1){
            String txt = String.format("안녕하세요! %s[%s]님.\n\n '인증하기'버튼을 클릭하여 이메일 인증을 완료해주세요.\n" +
                            "<a href=\"http://localhost:8090/user/auth?email=%s&authCd=%s\">인증하기</a>"
                    ,param.getNm(),param.getNick(), param.getEmail(),authCd);
            email.sendMimeMessage(param.getEmail(), myConst.SUBJECT, txt);
        }
    }

    //이메일 인증
   public int auth(UserEntity param) {
       return mapper.auth(param);
    }

    public int updMainProfile(MultipartFile[] profileImg) {
        if(profileImg==null){return 0;}
        int result = 0;
        UserEntity param = new UserEntity();
        param.setIuser(auth.getLoginUserPk());

        if(profileImg!=null && profileImg.length>0){
            String target = "user/"+param.getIuser();
            for(MultipartFile img : profileImg){
                String saveFileNm = myFileUtils.transferTo(img, target);
                if(saveFileNm != null){
                    param.setMainProfile(saveFileNm);
                    result = mapper.updMainProfile(param);
                }
            }
        }
        return result;
    }
}
