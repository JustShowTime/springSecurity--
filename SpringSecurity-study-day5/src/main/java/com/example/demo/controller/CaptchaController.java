package com.example.demo.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.code.kaptcha.Producer;

/**
 * @author   czq
 * @Date 2020-08-17 15:38:22    
 */
@Controller
public class CaptchaController {

	@Autowired
	private Producer captchaProducer;
	@RequestMapping(method = RequestMethod.GET,value = "/captcha.jpg")
	public void getCaptcha(HttpServletRequest request,HttpServletResponse response) throws IOException{
		response.setContentType("image/jpg");
		String capText=captchaProducer.createText();
		request.getSession().setAttribute("captcha", capText);
		BufferedImage bi=captchaProducer.createImage(capText);
		ServletOutputStream out=response.getOutputStream();
		ImageIO.write(bi, "jpg", out);
		try {
			out.flush();
		}finally {
			out.close();
		}
	}
}
