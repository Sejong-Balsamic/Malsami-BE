package com.balsamic.sejongmalsami.web.controller.view;

import com.balsamic.sejongmalsami.dto.ServerInfo;
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

    model.addAttribute("isLinuxServer", ServerInfo.isLinuxServer);
  }
}
