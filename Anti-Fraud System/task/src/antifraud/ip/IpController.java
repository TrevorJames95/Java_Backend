package antifraud.ip;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/antifraud")
@AllArgsConstructor
public class IpController {
    IPService ipService;

    @PostMapping("/suspicious-ip")
    Ip registerIp(@Valid @RequestBody Ip ip) {
        if (ipService.validateIpAddress(ip.getIp())) {
            Optional<Ip> dbIp = ipService.findByNumber(ip.getIp());
            if (dbIp.isPresent()) {
                throw  new ResponseStatusException(HttpStatus.CONFLICT);
            } else {
                return ipService.saveIpAddress(ip);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    Map<String, String> deleteCard(@PathVariable String ip) {
        //If the ip is invalid throws a Bad Request.
        if (!ipService.validateIpAddress(ip)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            if (ipService.deleteIp(ip)) {
                return Map.of("status", "IP " + ip + " successfully removed!");
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
    }

    @GetMapping("/suspicious-ip")
    List<Ip> getCards() {
        List<Ip> ips = ipService.listIps();
        if (ips == null) {
            return List.of();
        } else return ips;
    }
}
