package application;

@Controller
public class MachineController {
    @GetMapping("/client/update")
    public void update(String url) {
        Client.update();
    }
}
