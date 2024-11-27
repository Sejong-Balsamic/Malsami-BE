package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.service.QuestionPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/question")
@RequiredArgsConstructor
public class QuestionPageController {

  private final QuestionPostService questionPostService;

  @PostMapping(value = "/get/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String listQuestions(Model model) {
    QuestionDto dto = questionPostService.getAllQuestions();
    model.addAttribute("dto", dto);
    return "admin/question";
  }

  @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String saveQuestion(@ModelAttribute QuestionCommand command) {
    questionPostService.saveQuestion(command);
    return "redirect:/admin/question";
  }

  @PostMapping(value = "/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String editQuestionForm(
      Model model,
      @ModelAttribute QuestionCommand command) {
    QuestionDto dto = questionPostService.getQuestionById(command);
    model.addAttribute("dto", dto);
    return "admin/question-form";
  }

  @PostMapping(value = "/delete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String deleteQuestion(@ModelAttribute QuestionCommand command) {
    questionPostService.deleteQuestion(command);
    return "redirect:/admin/question";
  }
}
