/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.toobeetooteebot.util.cache.data.tab;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;
import net.daporkchop.toobeetooteebot.util.cache.CachedData;

import java.util.function.Consumer;

/**
 * @author DaPorkchop_
 */
@Getter
public class TabListCache implements CachedData {
    private TabList tabList = new TabList();

    @Override
    public void getPacketsSimple(Consumer<Packet> consumer) {
        consumer.accept(new ServerPlayerListDataPacket(this.tabList.getHeader(), this.tabList.getFooter()));
        consumer.accept(new ServerPlayerListEntryPacket(
                PlayerListEntryAction.ADD_PLAYER,
                this.tabList.getEntries().stream().map(PlayerEntry::toMCProtocolLibEntry).toArray(PlayerListEntry[]::new)
        ));
    }

    @Override
    public void reset() {
        this.tabList = new TabList();
    }

    @Override
    public String getSendingMessage() {
        return "Sending tab list";
    }
}
