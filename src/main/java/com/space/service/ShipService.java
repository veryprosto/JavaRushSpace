package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipService {
    private final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    private boolean isValidShip(Ship ship) {
        return isValidShipName(ship) &&
                isValidShipPlanet(ship) &&
                isValidShipProdDate(ship) &&
                isValidShipSpeed(ship) &&
                isValidShipCrewSize(ship) &&
                ship.getShipType() != null;
    }

    public Ship createShip(Ship ship) {
        if (!isValidShip(ship)) {
            throw new BadRequestException();
        } else if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        ship.setRating(getNewRating(ship));
        return shipRepository.save(ship);
    }

    public void delete(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }
        shipRepository.deleteById(id);
    }

    public Ship getShipById(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }
        return shipRepository.findById(id).orElse(null);
    }

    private boolean isValidShipName(Ship ship) {
        if (ship.getName() == null) return false;
        return ship.getName().length() <= 50 && !ship.getName().isEmpty();
    }

    private boolean isValidShipPlanet(Ship ship) {
        if (ship.getPlanet() == null) return false;
        return ship.getPlanet().length() <= 50 && !ship.getPlanet().isEmpty();
    }

    private boolean isValidShipProdDate(Ship ship) {
        if (ship.getProdDate() == null) return false;
        return ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() >= 2800 &&
                ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() <= 3019;
    }

    private boolean isValidShipSpeed(Ship ship) {
        if (ship.getSpeed() == null) return false;
        return ship.getSpeed() >= 0.01d && ship.getSpeed() <= 0.99d;
    }

    private boolean isValidShipCrewSize(Ship ship) {
        if (ship.getCrewSize() == null) return false;
        return ship.getCrewSize() >= 1 && ship.getCrewSize() <= 9999;
    }

    @Transactional
    public Ship updateShip(Ship newShip, Long id) {
        Ship oldShip = getShipById(id);

        if (newShip == null || oldShip == null) throw new BadRequestException();

        if (newShip.getName() != null) {
            if (!isValidShipName(newShip)) throw new BadRequestException();
            oldShip.setName(newShip.getName());
        }

        if (newShip.getPlanet() != null) {
            if (!isValidShipPlanet(newShip)) throw new BadRequestException();
            oldShip.setPlanet(newShip.getPlanet());
        }

        if (newShip.getShipType() != null) oldShip.setShipType(newShip.getShipType());

        if (newShip.getProdDate() != null) {
            if (!isValidShipProdDate(newShip)) throw new BadRequestException();
            oldShip.setProdDate(newShip.getProdDate());
        }

        if (newShip.getUsed() != null) oldShip.setUsed(newShip.getUsed());

        if (newShip.getSpeed() != null) {
            if (!isValidShipSpeed(newShip)) throw new BadRequestException();
            oldShip.setSpeed(newShip.getSpeed());
        }

        if (newShip.getCrewSize() != null) {
            if (!isValidShipCrewSize(newShip)) {
                throw new BadRequestException();
            }
            oldShip.setCrewSize(newShip.getCrewSize());
        }

        oldShip.setRating(getNewRating(oldShip));
        return shipRepository.save(oldShip);
    }

    public List<Ship> getShipList(String name, String planet, ShipType shipType, Long after, Long before,
                                  Boolean getUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                  Integer maxCrewSize, Double minRating, Double maxRating) {

        List<Ship> ships = shipRepository.findAll();

        ships = name != null ? ships.stream().filter(ship -> ship.getName().contains(name)).collect(Collectors.toList()) : ships;
        ships = planet != null ? ships.stream().filter(ship -> ship.getPlanet().contains(planet)).collect(Collectors.toList()) : ships;
        ships = shipType != null ? ships.stream().filter(ship -> ship.getShipType().equals(shipType)).collect(Collectors.toList()) : ships;
        ships = after != null ? ships.stream().filter(ship -> ship.getProdDate().after(new Date(after))).collect(Collectors.toList()) : ships;
        ships = before != null ? ships.stream().filter(ship -> ship.getProdDate().before(new Date(before))).collect(Collectors.toList()) : ships;
        ships = getUsed != null ? ships.stream().filter(ship -> ship.getUsed().equals(getUsed)).collect(Collectors.toList()) : ships;
        ships = minSpeed != null ? ships.stream().filter(ship -> ship.getSpeed() >= minSpeed).collect(Collectors.toList()) : ships;
        ships = maxSpeed != null ? ships.stream().filter(ship -> ship.getSpeed() <= maxSpeed).collect(Collectors.toList()) : ships;
        ships = minCrewSize != null ? ships.stream().filter(ship -> ship.getCrewSize() >= minCrewSize).collect(Collectors.toList()) : ships;
        ships = maxCrewSize != null ? ships.stream().filter(ship -> ship.getCrewSize() <= maxCrewSize).collect(Collectors.toList()) : ships;
        ships = minRating != null ? ships.stream().filter(ship -> ship.getRating() >= minRating).collect(Collectors.toList()) : ships;
        ships = maxRating != null ? ships.stream().filter(ship -> ship.getRating() <= maxRating).collect(Collectors.toList()) : ships;

        return ships;
    }

    public List<Ship> filteredShips(final List<Ship> shipList, ShipOrder order, Integer pageNumber, Integer pageSize) {
        pageNumber = pageNumber == null ? 0 : pageNumber;
        pageSize = pageSize == null ? 3 : pageSize;

        return shipList.stream()
                .sorted(getComparator(order))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    private Comparator<Ship> getComparator(ShipOrder order) {
        if (order == null) {
            return Comparator.comparing(Ship::getId);
        }

        Comparator<Ship> comparator = null;
        switch (order.getFieldName()) {
            case "id":
                comparator = Comparator.comparing(Ship::getId);
                break;
            case "speed":
                comparator = Comparator.comparing(Ship::getSpeed);
                break;
            case "prodDate":
                comparator = Comparator.comparing(Ship::getProdDate);
                break;
            case "rating":
                comparator = Comparator.comparing(Ship::getRating);
        }
        return comparator;
    }

    private Double getNewRating(Ship ship) {
        int productionDate = ship
                .getProdDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getYear();
        double rating = (80 * ship.getSpeed() * (ship.getUsed() ? 0.5d : 1.0d)) / (double) (3019 - productionDate + 1);
        return (double) Math.round(rating * 100) / 100;
    }


}


