package com.acc.somsomparty.domain.Festival.entity;

import com.acc.somsomparty.domain.Reservation.entity.Reservation;
import com.acc.somsomparty.domain.Ticket.entity.Ticket;
import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import com.acc.somsomparty.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "festival", indexes = {
        @Index(name = "idx_name_lower", columnList = "name_lower"),
        @Index(name = "idx_description_lower", columnList = "description_lower")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Festival extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "name_lower", nullable = false, length = 20)
    private String nameLower;

    @Column(name = "description_lower", nullable = false, columnDefinition = "TEXT")
    private String descriptionLower;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
    private List<Ticket> ticketList = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id")
    private ChatRoom chatRoom;

    public void addChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    @PrePersist
    @PreUpdate
    private void setLowercaseValues() {
        this.nameLower = this.name.toLowerCase();
        this.descriptionLower = this.description.toLowerCase();
    }

    public Festival(String name, String description, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
