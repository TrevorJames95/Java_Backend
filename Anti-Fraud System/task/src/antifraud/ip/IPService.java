package antifraud.ip;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class IPService {
    private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]" +
            "\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    private final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private final IPRepository ipRepository;

    public Ip saveIpAddress(Ip ip) {
        return ipRepository.save(ip);
    }

    public Optional<Ip> findByNumber(String number) {
        return ipRepository.findByNumber(number);
    }

    //Searches for the card and deletes it after its found.
    //Boolean helps to format the response request.
    @Transactional
    public boolean deleteIp(String number) {
        Optional<Ip> ip = ipRepository.findByNumber(number);
        if (ip.isPresent()) {
            ipRepository.deleteById(ip.get().getId());
            return true;
        } else {
            return false;
        }
    }

    public List<Ip> listIps() {
        return ipRepository.findAll(
                Sort.sort(Ip.class).by(Ip::getId).ascending()
        );
    }

    public boolean validateIpAddress(String number) {
        return IPV4_PATTERN.matcher(number).matches();
    }
}
