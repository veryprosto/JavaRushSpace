package com.space.controller;

import com.space.exception.BadRequestException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ResponseBody
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping(value = "/rest/ships")
    public List<Ship> getShipList(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String planet,
                                  @RequestParam(required = false) ShipType shipType,
                                  @RequestParam(required = false) Long after,
                                  @RequestParam(required = false) Long before,
                                  @RequestParam(required = false) Boolean isUsed,
                                  @RequestParam(required = false) Double minSpeed,
                                  @RequestParam(required = false) Double maxSpeed,
                                  @RequestParam(required = false) Integer minCrewSize,
                                  @RequestParam(required = false) Integer maxCrewSize,
                                  @RequestParam(required = false) Double minRating,
                                  @RequestParam(required = false) Double maxRating,
                                  @RequestParam(required = false) ShipOrder order,
                                  @RequestParam(required = false) Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize) {
        List<Ship> filteredShips = shipService.getShipList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        return shipService.filteredShips(filteredShips, order, pageNumber, pageSize);
    }

    @GetMapping(value = "/rest/ships/count")
    public int getShipCount(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String planet,
                            @RequestParam(required = false) ShipType shipType,
                            @RequestParam(required = false) Long after,
                            @RequestParam(required = false) Long before,
                            @RequestParam(required = false) Boolean isUsed,
                            @RequestParam(required = false) Double minSpeed,
                            @RequestParam(required = false) Double maxSpeed,
                            @RequestParam(required = false) Integer minCrewSize,
                            @RequestParam(required = false) Integer maxCrewSize,
                            @RequestParam(required = false) Double minRating,
                            @RequestParam(required = false) Double maxRating) {
        return shipService.getShipList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }

    @GetMapping(value = "/rest/ships/{id}")
    public Ship getShipById(@PathVariable Long id) {
        validShipId(id);
        return shipService.getShipById(id);
    }

    @PostMapping(value = "/rest/ships")
    @ResponseBody
    public Ship createShip(@RequestBody Ship ship) {
        Ship newShip = shipService.createShip(ship);
        if (newShip == null) throw new BadRequestException();
        return newShip;
    }

    @DeleteMapping(value = "/rest/ships/{id}")
    public void deleteShip(@PathVariable Long id) {
        validShipId(id);
        shipService.delete(id);
    }

    @PostMapping(value = "rest/ships/{id}")
    @ResponseBody
    public Ship updateShip(@RequestBody Ship ship, @PathVariable Long id) {
        validShipId(id);
        return shipService.updateShip(ship, id);
    }

    private void validShipId(Long id) {
        if (id == null || id != Math.floor(id) || id <= 0) throw new BadRequestException();
    }
}