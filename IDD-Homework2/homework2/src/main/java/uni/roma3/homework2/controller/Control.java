package uni.roma3.homework2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Control {
    private LuceneService luceneService = new LuceneService();

    @GetMapping("/")
    public String homePage(){
        return "index.html";
    }

    @GetMapping("/search")
    public String searchPages(Model model, @RequestParam(name="q") String query, @RequestParam(name="search_type") String searchType){
        model.addAttribute("query", query);
        model.addAttribute("searchType", searchType);
        System.out.println("submitted query");
        model.addAttribute("result", this.luceneService.search(searchType, query));
        System.out.println("finished query");
        return "index.html";
    }

}