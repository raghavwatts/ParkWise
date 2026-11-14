package com.parkwise.service;

import com.parkwise.dao.TariffDAO;
import com.parkwise.exception.InvalidTariffException;
import com.parkwise.model.Tariff;
import com.parkwise.model.VehicleType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service for tariff management and retrieval.
 */
public class TariffService {

    private final TariffDAO tariffDAO;

    public TariffService() {
        this.tariffDAO = new TariffDAO();
    }

    public List<Tariff> getAllTariffs() throws SQLException {
        return tariffDAO.findAll();
    }

    public Optional<Tariff> getTariffForType(VehicleType type) throws SQLException {
        return tariffDAO.findByVehicleType(type);
    }

    /**
     * Updates a tariff after validation.
     *
     * @throws InvalidTariffException if fee values are negative
     */
    public void updateTariff(Tariff tariff) throws SQLException, InvalidTariffException {
        if (tariff.getFirstHourFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTariffException("First hour fee cannot be negative");
        }
        if (tariff.getAdditionalHourFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTariffException("Additional hour fee cannot be negative");
        }
        tariffDAO.update(tariff);
    }
}
