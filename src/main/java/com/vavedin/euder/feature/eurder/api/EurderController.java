package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.CreateEurderDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.service.EurderService;
import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.security.enums.Feature;
import com.vavedin.euder.feature.user.domain.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/eurders")
public class EurderController {

    private final EurderService eurderService;
    private final SecurityService securityService;

    public EurderController(EurderService eurderService, SecurityService securityService) {
        this.eurderService = eurderService;
        this.securityService = securityService;
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "basicAuth")
    public EurderDto addEurder(@Parameter(hidden = true)
                               @RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestBody CreateEurderDto createEurderDto) {
        User loggedInUser = securityService.validateAuthorization(authorization, Feature.ORDER_ITEM);

        return eurderService.addEurder(createEurderDto, loggedInUser);
    }
}
