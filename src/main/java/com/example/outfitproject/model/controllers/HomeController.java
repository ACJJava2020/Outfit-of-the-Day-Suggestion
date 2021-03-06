package com.example.outfitproject.model.controllers;

import com.cloudinary.utils.ObjectUtils;
import com.example.outfitproject.main.entity.Category;
import com.example.outfitproject.main.entity.Climate;
import com.example.outfitproject.main.entity.Item;
import com.example.outfitproject.main.entity.User;
import com.example.outfitproject.main.entity.repositories.CategoryRepository;
import com.example.outfitproject.main.entity.repositories.ClimateRepository;
import com.example.outfitproject.main.entity.repositories.ItemRepository;
import com.example.outfitproject.main.entity.repositories.UserRepository;
import com.example.outfitproject.main.services.UserService;
import com.example.outfitproject.model.config.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ClimateRepository climateRepository;

    @ModelAttribute("categories")
    public Iterable<Category> populateCategories() {
        return categoryRepository.findAll();
    }

    @ModelAttribute("climates")
    public Iterable<Climate> populateClimates() {
        return climateRepository.findAll();
    }

    @GetMapping("/add-item")
    public String addItem(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("climates", climateRepository.findAll());
        model.addAttribute("item", new Item());
        return "itemform";
    }

    @PostMapping("/process")
    public String processForm(@Valid @ModelAttribute("item") Item item,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              Model model) {

        model.addAttribute("image", "Upload Image");
        model.addAttribute("myuser", userService.getUser());
        if (result.hasErrors()) {
            return "itemform";
        }
        if (!file.isEmpty()) {
            try {

                Map uploadResultMap = cloudc.upload(
                        file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                item.setPicture( uploadResultMap.get("url").toString());
                item.setUser(userService.getUser());
                itemRepository.save(item);

            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/add-item";
            }
        } else {

            if (result.hasErrors()) {
                return "itemform";
            }
        }
        item.setUser(userService.getUser());
        itemRepository.save(item);
        return "redirect:/";
    }

    @RequestMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("user", userRepository.findByUsername(username));
        if (userService.isUser()) {
            model.addAttribute("items", itemRepository.findAllByUser(userService.getUser()));
        }
        if (userService.isAdmin()) {
            model.addAttribute("items", itemRepository.findAll());
        }
        return "profile";
    }

    @RequestMapping("/update/{id}")
    public String updateItem(@PathVariable("id") long id, Model model) {
        model.addAttribute("item", itemRepository.findById(id).get());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("climates", climateRepository.findAll());
        return "updateitem";
    }

    @RequestMapping("/delete/{id}")
    public String delItem(@PathVariable("id") long id) {
        itemRepository.deleteById(id);
        return "redirect:/profile";
    }

    @RequestMapping("/login?logout")
    public String logout() {
        return "redirect:/";
    }

    @PostMapping("/update-item")
    public String processUpdateItem(@Valid @ModelAttribute("item") Item item, long id,
                                    BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              Model model) {
        model.addAttribute("image", "Upload Image");
        model.addAttribute("myuser", userService.getUser());
        if (result.hasErrors()) {
            return "updateitem";
        }
        if (file.isEmpty()) {
            item.setPicture(itemRepository.findById(id).get().getPicture());
        } else {
            try {

                Map uploadResultMap = cloudc.upload(
                        file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                item.setPicture(uploadResultMap.get("url").toString());
                item.setUser(userService.getUser());
                itemRepository.save(item);

            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/";
            }
        }
        item.setUser(userService.getUser());
        itemRepository.save(item);
        return "redirect:/profile";
    }



}
