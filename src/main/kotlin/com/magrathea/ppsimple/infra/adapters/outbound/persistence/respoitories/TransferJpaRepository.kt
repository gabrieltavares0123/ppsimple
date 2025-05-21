package com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories

import com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities.TransferJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TransferJpaRepository : JpaRepository<TransferJpaEntity, Int>