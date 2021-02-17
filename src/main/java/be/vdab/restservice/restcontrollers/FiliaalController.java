package be.vdab.restservice.restcontrollers;

import be.vdab.restservice.domain.Filiaal;
import be.vdab.restservice.exceptions.FiliaalNietGevondenException;
import be.vdab.restservice.services.FiliaalService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/filialen")
@ExposesResourceFor(Filiaal.class)
@CrossOrigin(exposedHeaders = "Location")
class FiliaalController {
    private final FiliaalService filiaalService;
    private final EntityLinks entityLinks;

    public FiliaalController(FiliaalService filiaalService, EntityLinks entityLinks) {  //foutmelding is verkeerd/ negeren
        this.filiaalService = filiaalService;
        this.entityLinks = entityLinks;
    }
    @GetMapping("{id}")
    @Operation(summary = "Een filiaal zoeken op id")
    @CrossOrigin
    EntityModel<Filiaal> get(@PathVariable long id) {
        return filiaalService.findById(id).map(filiaal -> EntityModel.of(
                filiaal,
                entityLinks.linkToItemResource(Filiaal.class, filiaal.getId()),
                entityLinks.linkForItemResource(Filiaal.class, filiaal.getId())
                        .slash("werknemers").withRel("werknemers")))
 .orElseThrow(FiliaalNietGevondenException::new);
    }
    @ExceptionHandler(FiliaalNietGevondenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void filiaalNietGevonden() {
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Een filiaal verwijderen")
    void delete(@PathVariable long id) {
        filiaalService.delete(id);
    }


    @PostMapping
    @Operation(summary = "Een filiaal toevoegen")
    @ResponseStatus(HttpStatus.CREATED)
    HttpHeaders create(@RequestBody @Valid Filiaal filiaal) {
        filiaalService.create(filiaal);
        var link = entityLinks.linkToItemResource(Filiaal.class, filiaal.getId());
        var headers = new HttpHeaders();
        headers.setLocation(link.toUri());
        return headers;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String,String> verkeerdeData(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
 .collect(Collectors.toMap(FieldError::getField,
                FieldError::getDefaultMessage));
    }

    @PutMapping("{id}")
    @Operation(summary = "Een filiaal wijzigen")
    void put(@RequestBody @Valid Filiaal filiaal) {
        filiaalService.update(filiaal);
    }


    @GetMapping
    @Operation(summary = "Alle filialen zoeken")

    CollectionModel<EntityModel<FiliaalIdNaam>> findAll() {
        return CollectionModel.of(
                filiaalService.findAll().stream()
                .map(filiaal ->
                        EntityModel.of(new FiliaalIdNaam(filiaal),
                                entityLinks.linkToItemResource(Filiaal.class, filiaal.getId())))
 .collect(Collectors.toList()),
        entityLinks.linkToCollectionResource(Filiaal.class));
    }


    private static class FiliaalIdNaam {
        private final long id;
        private final String naam;
        public FiliaalIdNaam(Filiaal filiaal) {
            id = filiaal.getId();
            naam = filiaal.getNaam();
        }

        public long getId() {
            return id;
        }

        public String getNaam() {
            return naam;
        }
    }
}