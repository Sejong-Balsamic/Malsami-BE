package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.util.config.ServerConfig;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

  /**
   * boolean isLinuxServer 전달
   */
  @ModelAttribute
  public void addServerConfig(Model model) {

    model.addAttribute("isLinuxServer", ServerConfig.isLinuxServer);
  }
}
